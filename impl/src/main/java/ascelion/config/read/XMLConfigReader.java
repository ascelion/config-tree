
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ascelion.config.api.ConfigReader;

import static ascelion.config.conv.Utils.path;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@ConfigReader.Type( value = XMLConfigReader.TYPE )
public class XMLConfigReader extends ResourceReader
{

	static public final String TYPE = "XML";

	static class Context
	{

		final Map<String, String> map;
		final Context parent;
		final String prefix;

		Context( Map<String, String> map )
		{
			this.map = map;
			this.parent = null;
			this.prefix = null;
		}

		Context( Map<String, String> map, Context parent, String name )
		{
			this.map = map;
			this.parent = parent;
			this.prefix = path( parent.prefix, name );
		}

		void set( String name, String value )
		{
			this.map.put( path( this.prefix, name ), value );
		}

		void set( String value )
		{
			this.map.put( this.prefix, value );
		}
	}

	static class Handler extends DefaultHandler
	{

		final Map<String, String> map = new TreeMap<>();

		private Context context;
		private String content;

		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
		{
			if( this.context == null ) {
				this.context = new Context( this.map );
			}
			else {
				this.context = new Context( this.map, this.context, qName );
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
	protected Map<String, String> readConfiguration( InputStream is ) throws IOException
	{
		try {
			final SAXParserFactory f = SAXParserFactory.newInstance();
			final SAXParser p = f.newSAXParser();
			final Handler h = new Handler();

			p.parse( is, h );

			return h.map;
		}
		catch( final ParserConfigurationException | SAXException x ) {
			throw new IOException( x );
		}
	}

}
