/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.jndi.deployers.proxy;

import org.jboss.ejb3.jndi.binder.impl.AbstractLazyProxyFactory;
import org.jboss.ejb3.jndi.binder.impl.View;
import org.jboss.ejb3.jndi.deployers.metadata.SessionBeanTypeWrapper;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedSessionBean31JNDINameResolver;

/**
 * Lazy proxy factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LazyProxyFactory extends AbstractLazyProxyFactory
{
   private JNDIPolicyBasedSessionBean31JNDINameResolver nameResolver = new JNDIPolicyBasedSessionBean31JNDINameResolver();

   @Override
   public Object produce(View view)
   {
      SessionBeanTypeWrapper beanType = (SessionBeanTypeWrapper) view.getMetadata();
      String className = view.getBusinessInterface().getName();
      String linkName = nameResolver.resolveJNDIName(beanType.getSessionBeanMetaData(), className);
      return lazyLinkRef(className, linkName);
   }
}
