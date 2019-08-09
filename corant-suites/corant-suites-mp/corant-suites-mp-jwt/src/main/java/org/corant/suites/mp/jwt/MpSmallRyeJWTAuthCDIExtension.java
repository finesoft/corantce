/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.suites.mp.jwt;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.jboss.logging.Logger;
import io.smallrye.jwt.auth.cdi.ClaimValueProducer;
import io.smallrye.jwt.auth.cdi.CommonJwtProducer;
import io.smallrye.jwt.auth.cdi.JsonValueProducer;
import io.smallrye.jwt.auth.cdi.PrincipalProducer;
import io.smallrye.jwt.auth.cdi.RawClaimTypeProducer;
import io.smallrye.jwt.config.JWTAuthContextInfoProvider;

/**
 * corant-suites-mp-jwt
 *
 * @author bingo 上午10:28:45
 *
 */
public class MpSmallRyeJWTAuthCDIExtension implements Extension {

  private static Logger logger = Logger.getLogger(MpSmallRyeJWTAuthCDIExtension.class);

  void addAnnotatedType(BeforeBeanDiscovery event, BeanManager beanManager, Class<?> type) {
    final String id = "SmallRye" + type.getSimpleName();
    event.addAnnotatedType(beanManager.createAnnotatedType(type), id);
    logger.debugf("Added type: %s", type.getName());
  }

  void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
    logger.debugf("beanManager = %s", beanManager);

    // TODO: Do not add CDI beans unless @LoginConfig (or other trigger) is configured
    addAnnotatedType(event, beanManager, MpJaxRsFeature.class);
    addAnnotatedType(event, beanManager, ClaimValueProducer.class);
    addAnnotatedType(event, beanManager, CommonJwtProducer.class);
    addAnnotatedType(event, beanManager, JsonValueProducer.class);
    addAnnotatedType(event, beanManager, JWTAuthContextInfoProvider.class);
    addAnnotatedType(event, beanManager, PrincipalProducer.class);
    // addAnnotatedType(event, beanManager, MpJWTAuthenticationFilter.class);
    addAnnotatedType(event, beanManager, RawClaimTypeProducer.class);
  }
}