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
package org.corant.modules.microprofile.jwt.jaxrs;

import static org.corant.context.Beans.find;
import static org.corant.shared.util.Empties.isEmpty;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.corant.context.security.SecurityContexts;
import org.corant.modules.microprofile.jwt.MpJWTAuthorizer;
import org.corant.modules.security.Authorizer;
import org.corant.modules.security.shared.SimpleRoles;

/**
 * corant-modules-microprofile-jwt
 *
 * @author bingo 下午7:52:30
 *
 */
@Priority(Priorities.AUTHORIZATION)
public class MpJWTRolesAllowedFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private final SimpleRoles allowedRoles;

  volatile Authorizer authorizer;

  public MpJWTRolesAllowedFilter(String... allowedRoles) {
    if (isEmpty(allowedRoles)) {
      this.allowedRoles = SimpleRoles.of(MpJWTAuthorizer.ALL_ROLES);
    } else {
      this.allowedRoles = SimpleRoles.of(allowedRoles);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    try {
      authorizer().checkAccess(SecurityContexts.getCurrent(), allowedRoles);
    } catch (Exception e) {
      if (requestContext.getSecurityContext().getUserPrincipal() == null) {
        Object ex = requestContext.getProperty(MpJWTAuthenticationFilter.JTW_EXCEPTION_KEY);
        if (ex instanceof Exception) {
          requestContext.removeProperty(MpJWTAuthenticationFilter.JTW_EXCEPTION_KEY);
          e.addSuppressed((Exception) ex);
        }
        throw new NotAuthorizedException(e, "Bearer");
      } else {
        throw new ForbiddenException(e);
      }
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    authorizer.postCheckAccess();
  }

  protected Authorizer authorizer() {
    if (authorizer == null) {
      synchronized (this) {
        if (authorizer == null) {
          authorizer = find(Authorizer.class).orElse(MpJWTAuthorizer.DFLT_INST);
        }
      }
    }
    return authorizer;
  }
}
