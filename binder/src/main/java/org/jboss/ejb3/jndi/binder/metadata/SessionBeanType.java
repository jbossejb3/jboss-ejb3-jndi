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
package org.jboss.ejb3.jndi.binder.metadata;

import org.jboss.reloaded.naming.spi.JavaEEComponent;

import java.util.Collection;

/**
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public interface SessionBeanType extends JavaEEComponent
{
   /**
    * EJB 3.1 3.4.3: Session Bean's Business Interface
    *
    * The session bean's business local interfaces.
    * @return the business local interfaces or null
    */
   Collection<Class<?>> getBusinessLocals();

   /**
    * EJB 3.1 3.4.3: Session Bean's Business Interface
    *
    * The session bean's business remote interfaces.
    * @return the business remote interfaces or null
    */
   Collection<Class<?>> getBusinessRemotes();

   /**
    * The bean class for this enterprise bean.
    * @return the bean class
    */
   Class<?> getEJBClass();
   
   /**
    * EJB 3.1 3.6.2: Session Bean's Remote Home Interface
    *
    * The enterprise bean's remote home interface.
    * @return the remote home interface or null
    */
   Class<?> getHome();

   /**
    * The enterprise bean's remote interface.
    * @return the remote interface or null
    */
   Class<?> getRemote();

   /**
    * The enterprise bean's local interface.
    * @return the local interface or null
    */
   Class<?> getLocal();

   /**
    * EJB 3.1 3.6.3: Session Bean's Local Home Interface
    *
    * The enterprise bean's local home interface.
    * @return the local home interface or null
    */
   Class<?> getLocalHome();

   /**
    * EJB 3.1 3.4.4: Session Bean's No-Interface View
    *
    * Declares that this session bean exposes a no-interface Local view.
    */
   boolean isLocalBean();
}
