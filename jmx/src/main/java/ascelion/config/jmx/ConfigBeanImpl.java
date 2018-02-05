
package ascelion.config.jmx;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import ascelion.config.utils.Expression;

import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Getter;

class ConfigBeanImpl extends NotificationBroadcasterSupport implements ConfigBean
{

	static private final Random RND = new SecureRandom();
	static private ThreadLocal<Boolean> UNPROTECTED = new ThreadLocal<Boolean>()
	{

		@Override
		protected Boolean initialValue()
		{
			return false;
		}
	};

	static <T> T unprotected( Supplier<T> action )
	{
		UNPROTECTED.set( true );

		try {
			return action.get();
		}
		finally {
			UNPROTECTED.remove();
		}
	}

	@Getter
	private final String path;
	private final UnaryOperator<String> lookup;
	private String expression;
	private SecretKeySpec secret;
	private long ntf_seq;
	@Getter
	private boolean modified;

	ConfigBeanImpl( String path, boolean sensitive, UnaryOperator<String> lookup )
	{
		this.path = path;
		this.lookup = lookup;

		byte[] password;
		if( sensitive ) {
			password = new byte[16];

			RND.nextBytes( password );

			this.secret = new SecretKeySpec( password, "AES" );
		}
		else {
			this.secret = null;
		}

		this.expression = encode( lookup.apply( path ) );
	}

	void setExpression( String expression )
	{
		expression = encode( expression );

		if( !Objects.equals( this.expression, expression ) ) {
			final String oldValue = this.expression;

			this.expression = expression;

			final Notification n = new AttributeChangeNotification( this, this.ntf_seq++, System.currentTimeMillis(), "Configuration changed", getPath(), "java.lang.String", oldValue, this.expression );

			sendNotification( n );

			this.modified = true;
		}
	}

	@Override
	public String getExpression()
	{
		return this.expression;
	}

	@Override
	public String getValue()
	{
		final Expression exp = new Expression( this.lookup, decode( this.expression ) );

		return UNPROTECTED.get() ? exp.getValue() : encode( exp.getValue() );
	}

	@Override
	public String getDefaultValue()
	{
		final Expression exp = new Expression( this.lookup, decode( this.expression ) );

		return UNPROTECTED.get() ? exp.getDefValue() : encode( exp.getDefValue() );
	}

	private final String encode( String value )
	{
		if( isBlank( value ) || this.secret == null ) {
			return value;
		}

		try {
			final Cipher cip = Cipher.getInstance( "AES" );

			cip.init( Cipher.ENCRYPT_MODE, this.secret );

			final Encoder enc = Base64.getEncoder();
			final byte[] buf = cip.doFinal( value.getBytes( "UTF-8" ) );

			return enc.encodeToString( buf );
		}
		catch( final GeneralSecurityException e ) {
			throw new IllegalStateException( e );
		}
		catch( final UnsupportedEncodingException e ) {
			throw new IllegalStateException( e );
		}
	}

	private final String decode( String value )
	{
		if( isBlank( value ) || this.secret == null ) {
			return value;
		}

		try {
			final Cipher cip = Cipher.getInstance( "AES" );

			cip.init( Cipher.DECRYPT_MODE, this.secret );

			final Decoder dec = Base64.getDecoder();
			final byte[] buf = cip.doFinal( dec.decode( value ) );

			return new String( buf, "UTF-8" );
		}
		catch( final GeneralSecurityException e ) {
			throw new IllegalStateException( e );
		}
		catch( final UnsupportedEncodingException e ) {
			throw new IllegalStateException( e );
		}
	}
}
