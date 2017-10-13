
package ascelion.shared.cdi.conf;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;

@RunWith( CdiTestRunner.class )
public class ConfigValueTest
{

	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		LogManager.getLogManager().reset();
	}

	@Inject
	@ConfigValue( "log.file1:${sys.user.dir}/file1.log" )
	private File logFile1;

	@ConfigValue( "log.file2:${env.user.dir}/file2.log" )
	private File logFile2;

	@ConfigValue( "log.categories" )
	private List<String> logCategories;

	@ConfigValue( value = "log.mappings", unwrap = "log" )
	private Map<String, Object> logMappings;

	@Test
	public void run()
	{
		assertNotNull( "logFile1", this.logFile1 );
		assertNotNull( "logFile2", this.logFile2 );
	}
}
