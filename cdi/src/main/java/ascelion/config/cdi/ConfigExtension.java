package ascelion.config.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;

import ascelion.config.annotations.ConfigValue;

import static ascelion.cdi.metadata.AnnotatedTypeModifier.makeQualifier;

public class ConfigExtension implements Extension {

	private AnnotatedType<ConfigProducer> prodType;
	private Bean<ConfigProducer> prodBean;

	void beforeBeanDiscovery(BeanManager bm, @Observes BeforeBeanDiscovery event) {
		event.addQualifier(makeQualifier(bm.createAnnotatedType(ConfigValue.class)));

		this.prodType = bm.createAnnotatedType(ConfigProducer.class);

		event.addAnnotatedType(this.prodType, ConfigProducer.class.getName());
	}

	void processConfigProducerBean(@Observes ProcessManagedBean<ConfigProducer> event) {
		this.prodBean = event.getBean();
	}

}
