//
//package ascelion.shared.cdi.conf;
//
//import java.util.Set;
//
//import javax.enterprise.inject.Typed;
//import javax.enterprise.inject.spi.AnnotatedMember;
//import javax.enterprise.inject.spi.AnnotatedType;
//import javax.enterprise.util.AnnotationLiteral;
//import javax.inject.Inject;
//
//@Typed
//final class ConfigType<X> extends AnnotatedTypeW<X>
//{
//
//	static class InjectLiteral extends AnnotationLiteral<Inject> implements Inject
//	{
//	}
//
//	private boolean modified;
//
//	ConfigType( AnnotatedType<X> delegate )
//	{
//		super( delegate );
//
//		processMembers( getConstructors() );
//		processMembers( getFields() );
//		processMembers( getMethods() );
//	}
//
//	private <M extends AnnotatedMember<? super X>> void processMembers( Set<M> members )
//	{
//		members.stream()
//			.filter( m -> ConfigExtension.isConfigValue( m.getJavaMember() ) )
//			.filter( m -> !m.isAnnotationPresent( Inject.class ) )
//			.forEach( m -> {
//				m.getAnnotations().add( new InjectLiteral() );
//
//				this.modified = true;
//			} );
//		;
//	}
//
//	boolean modified()
//	{
//		return this.modified;
//	}
//}
