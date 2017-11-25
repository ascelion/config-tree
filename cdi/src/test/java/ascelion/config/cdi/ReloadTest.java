package ascelion.config.cdi;
//
//package ascelion.config.impl;
//
//import javax.enterprise.context.Dependent;
//import javax.enterprise.event.Event;
//import javax.enterprise.inject.Instance;
//import javax.inject.Inject;
//
//import ascelion.config.api.ConfigNode;
//import ascelion.config.api.ConfigReader;
//import ascelion.config.api.ConfigSource;
//import ascelion.config.api.ConfigValue;
//import ascelion.tests.cdi.CdiUnit;
//
//import static java.lang.String.format;
//import static java.util.Collections.singletonMap;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//import static org.junit.Assert.assertThat;
//
//import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
//import org.jglue.cdiunit.AdditionalClasses;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith( CdiUnit.class )
//@UseConfigExtension
//@AdditionalClasses( {
//	ReloadTest.Bean.class,
//	ReloadTest.CustomSource1.class,
//	ReloadTest.CustomSource2.class,
//} )
//public class ReloadTest
//{
//
//	static final long INTERVAL = 500;
//
//	@Dependent
//	@ConfigReader.Type( "INC1" )
//	@ConfigSource( type = "INC1" )
//	static class CustomSource1 implements ConfigReader
//	{
//
//		int index;
//
//		@Override
//		public void readConfiguration( ConfigSource source, ConfigNode root )
//		{
//			root.setValue( "reload1", format( "reload%01d", this.index++ ) );
//		}
//	}
//
//	@Dependent
//	@ConfigReader.Type( value = "INC2" )
//	@ConfigSource( type = "INC2" )
//	static class CustomSource2 implements ConfigReader
//	{
//
//		int index;
//
//		@Override
//		public void readConfiguration( ConfigSource source, ConfigNode root )
//		{
//			root.setValue( "reload2", format( "reload%01d", this.index++ ) );
//		}
//	}
//
//	static class Bean
//	{
//
//		@ConfigValue
//		String reload1;
//
//		@ConfigValue
//		String reload2;
//	}
//
//	@Inject
//	private Instance<Bean> inst;
//
//	@Inject
//	Instance<CustomSource1> csi1;
//
//	@Inject
//	Instance<CustomSource2> csi2;
//
//	@Inject
//	Event<ConfigSource> event;
//
//	@Test
//	public void run() throws InterruptedException
//	{
//		final Bean b1 = this.inst.get();
//		final Bean b2 = this.inst.get();
//
//		this.event.fire( AnnotationInstanceProvider.of( ConfigSource.class, singletonMap( "type", "INC1" ) ) );
//
//		final Bean b3 = this.inst.get();
//		final Bean b4 = this.inst.get();
//
//		this.event.fire( AnnotationInstanceProvider.of( ConfigSource.class, singletonMap( "type", "INC2" ) ) );
//
//		final Bean b5 = this.inst.get();
//		final Bean b6 = this.inst.get();
//
//		assertThat( b1.reload1, is( b2.reload1 ) );
//		assertThat( b1.reload2, is( b2.reload2 ) );
//
//		assertThat( b1.reload1, is( not( b3.reload1 ) ) );
//		assertThat( b1.reload2, is( b4.reload2 ) );
//
//		assertThat( b1.reload1, is( not( b5.reload1 ) ) );
//		assertThat( b1.reload2, is( not( b6.reload2 ) ) );
//	}
//}