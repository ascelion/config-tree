
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import static ascelion.shared.cdi.conf.ConfigNode.path;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@ConfigSource.Type( value = "XML" )
@ApplicationScoped
class XMLConfigReader implements ConfigReader
{

	class Context
	{

		final BiConsumer<String, Object> action;

		final Context parent;
		final String prefix;

		Context( BiConsumer<String, Object> action )
		{
			this.action = action;
			this.parent = null;
			this.prefix = null;
		}

		Context( BiConsumer<String, Object> action, Context parent, String name )
		{
			this.action = action;
			this.parent = parent;
			this.prefix = path( parent.prefix, name );
		}

		void set( String name, String value )
		{
			this.action.accept( path( this.prefix, name ), value );
		}

		void set( String value )
		{
			this.action.accept( this.prefix, value );
		}
	}

	class Handler extends DefaultHandler
	{

		final BiConsumer<String, Object> action;

		private Context context;
		private String content;

		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
		{
			if( this.context == null ) {
				this.context = new Context( this.action );
			}
			else {
				this.context = new Context( this.action, this.context, qName );
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

		Handler( BiConsumer<String, Object> action )
		{
			this.action = action;
		}
	}

	@Override
	public void readConfiguration( ConfigNode root, InputStream source ) throws IOException
	{
		try {
			final SAXParserFactory f = SAXParserFactory.newInstance();
			final SAXParser p = f.newSAXParser();

			p.parse( source, new Handler( ( k, v ) -> root.set( k, v ) ) );
		}
		catch( final ParserConfigurationException | SAXException x ) {
			throw new IOException( x );
		}
	}

}
