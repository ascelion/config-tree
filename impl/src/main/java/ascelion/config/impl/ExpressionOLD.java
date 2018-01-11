
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode.Kind;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@Deprecated
public final class ExpressionOLD extends Evaluable
{

	public static ExpressionOLD compile( String content )
	{
		return ContentParser.parse( content, ExpressionOLD.Listener::new );
	}

	static private class Listener implements ContentParser.Listener<ExpressionOLD>
	{

		private ExpressionOLD expr;

		@Override
		public void start()
		{
			this.expr = new ExpressionOLD();
		}

		@Override
		public void seen( Token tok )
		{
			if( this.expr == null ) {
				throw new ConfigParseException( "unknown error", asList( new ConfigParsePosition( "unbalanced '}'", tok.offset ) ) );
			}

			this.expr = this.expr.seen( tok );
		}

		@Override
		public ExpressionOLD finish()
		{
			return this.expr;
		}
	}

	static private final ThreadLocal<Deque<ExpressionOLD>> CHAIN = new ThreadLocal<Deque<ExpressionOLD>>()
	{

		@Override
		protected Deque<ExpressionOLD> initialValue()
		{
			return new LinkedList<>();
		};
	};

	final List<ExpressionItem> children = new ArrayList<>();
	final List<Evaluable> val = new ArrayList<>();
	final List<Evaluable> def = new ArrayList<>();
	List<Evaluable> add = this.val;

	@Override
	public String toString()
	{
		return this.children.stream().map( Object::toString ).collect( joining() );
	}

	<T extends ExpressionItem> T addChild( T child )
	{
		child.parent = this;

		this.children.add( child );

		if( child instanceof Evaluable ) {
			this.add.add( (Evaluable) child );
		}

		return child;
	}

	@Override
	CachedItem eval( ConfigNodeImpl node )
	{
		final StringBuilder b = new StringBuilder();

		b.insert( 0, this );

		CHAIN.get().forEach( x -> {
			b.insert( 0, "->" );
			b.insert( 0, x );
			if( Objects.equals( this, x ) ) {
				throw new ConfigLoopException( format( "recursive definition: %s", b ) );
			}
		} );

		CachedItem item = eval( this.val, node );

		if( isEvaluable() ) {
			CHAIN.get().push( this );

			try {
				final ConfigNodeImpl found = node.root.findNode( (String) item.cached(), false );

				if( found == null ) {
					item = new CachedItem( node );
				}
				else {
					item = found.item();

					// force evaluation before ending eval recursively
					item.cached();
				}
			}
			finally {
				CHAIN.get().pop();
			}
		}

		if( item.kind() == Kind.NULL ) {
			item = eval( this.def, node );
			item = new CachedItem( item.value(), item.node(), true );
		}

		if( isEvaluable() && item.kind() == Kind.NULL ) {
			throw new ConfigNotFoundException( toString() );
		}

		return item;
	}

	boolean isExpression()
	{
		return this.children.stream().anyMatch( c -> {
			return TypeItem.class.isInstance( c ) || ExpressionOLD.class.isInstance( c );
		} );
	}

	@Override
	boolean isEvaluable()
	{
		return this.val.size() == 1 && this.children.size() > 2;
	}

	public Set<String> evaluables()
	{
		final Set<String> set = new HashSet<>();

		evaluables( Stream.of( this ), set );

		return set;
	}

	private void evaluables( Stream<? extends Evaluable> stm, Set<String> set )
	{
		stm.filter( ExpressionOLD.class::isInstance )
			.map( ExpressionOLD.class::cast )
			.forEach( e -> {
				if( e.isEvaluable() ) {
					set.add( e.val.get( 0 ).toString() );
				}
				else {
					evaluables( e.val.stream(), set );
				}

				evaluables( e.def.stream(), set );
			} );
	}

	private CachedItem eval( List<Evaluable> elements, ConfigNodeImpl node )
	{
		switch( elements.size() ) {
			case 0:
				return new CachedItem( node );

			case 1:
				return elements.get( 0 ).eval( node );

			default:
				final String item = elements.stream()
					.map( e -> {
						final CachedItem v = e.eval( node );

						if( v.kind() != Kind.ITEM ) {
							throw new ConfigNotFoundException( e.toString() );
						}

						return (String) v.cached();
					} )
					.collect( joining() );

				return new CachedItem( item, node );
		}
	}

	private ExpressionOLD seen( Token tok )
	{
		switch( tok.type ) {
			case BEG: {
				final ExpressionOLD expr = addChild( new ExpressionOLD() );

				expr.addChild( new TypeItem( Token.Type.BEG ) );

				return expr;
			}

			case END:
				addChild( new TypeItem( tok.type ) );

				return this.parent;

			case DEF:
				addChild( new TypeItem( tok.type ) );

				this.add = this.def;
				;
			break;

			case STR: {
				addChild( new TextItem( tok.text ) );
			}
			break;
		}

		return this;
	}
}
