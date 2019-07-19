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

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import io.smallrye.jwt.auth.jaxrs.JWTAuthenticationFilter;

/**
 * corant-suites-mp-jwt
 *
 * @author bingo 下午7:36:14
 *
 */
@PreMatching
@Priority(Priorities.AUTHENTICATION)
@Provider
@ApplicationScoped
public class MpJWTAuthenticationFilter extends JWTAuthenticationFilter {

}
