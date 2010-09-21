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
package org.jboss.ejb3.jndi.binder.test.common;

import org.jboss.reloaded.naming.service.NameSpaces;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.NamingServer;
import org.jnp.server.SingletonNamingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public abstract class AbstractNamingTestCase
{
   private static SingletonNamingServer server;
   private static NameSpaces ns;
   protected static InitialContext iniCtx;
   protected static Context javaGlobal;

   @AfterClass
   public static void afterClass() throws NamingException
   {
      iniCtx.close();

      ns.stop();

      server.destroy();
   }

   @BeforeClass
   public static void beforeClass() throws NamingException
   {
      server = new SingletonNamingServer();

      ns = new NameSpaces();
      ns.start();

      iniCtx = new InitialContext();

      javaGlobal = (Context) iniCtx.lookup("java:global");
   }

   protected static Context createContext() throws NamingException
   {
      return createContext(iniCtx.getEnvironment());
   }

   protected static Context createContext(Hashtable<?, ?> environment) throws NamingException
   {
      NamingServer srv = new NamingServer();
      return new NamingContext(environment, null, srv);
   }

   /*
   protected static JavaEEModule createStandaloneModule(String moduleName) throws NamingException
   {
      Context context = javaGlobal.createSubcontext(moduleName);
      // standalone == app
      JavaEEApplication application = new SimpleJavaEEApplication(moduleName, context);
      JavaEEModule module = new SimpleJavaEEModule(moduleName, context, application);
      return module;
   }
   */
}
