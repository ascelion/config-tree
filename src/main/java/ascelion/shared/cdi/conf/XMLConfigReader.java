
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@ConfigSource.Type( value = "XML" )
@ApplicationScoped
class XMLConfigReader extends ConfigStore implements ConfigReader
{

	class Context
	{

		final Context parent;
		final String prefix;

		Context()
		{
			this.parent = null;
			this.prefix = null;
		}

		Context( Context parent, String prefix )
		{
			this.parent = parent;
			this.prefix = parent.prefix != null ? format( "%s.%s", parent.prefix, prefix ) : prefix;
		}

		void set( String name, String value )
		{
			setValue( this.prefix != null ? format( "%s.%s", this.prefix, name ) : name, value );
		}

		void set( String value )
		{
			setValue( this.prefix, value );
		}
	}

	class Handler extends DefaultHandler
	{

		private Context context;
		private String content;

		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
		{
			if( this.context == null ) {
				this.context = new Context();
			}
			else {
				this.context = new Context( this.context, qName );
			}

			for( int n = 0; n < attributes.getLength(); n++ ) {
				this.context.set( attributes.getQName( n ), attributes.getValue( n ) );
			}
		}

		@Override
		public void characters( char[] ch, int start, int length ) throws SAXException
		{
			this.content = trimToNull( new String( ch, start, length ) );
		}

		@Override
		public void endElement( String uri, String localName, String qName ) throws SAXException
		{
			if( this.content != null ) {
				this.context.set( this.content );
			}

			this.context = this.context.parent;
		}
	}

	@Override
	public Map<String, Object> readConfiguration( InputStream source ) throws IOException
	{
		try {
			final SAXParserFactory f = SAXParserFactory.newInstance();
			final SAXParser p = f.newSAXParser();

			p.parse( source, new Handler() );

			return get();
		}
		catch( final ParserConfigurationException | SAXException x ) {
			throw new IOException( x );
		}
	}

}
