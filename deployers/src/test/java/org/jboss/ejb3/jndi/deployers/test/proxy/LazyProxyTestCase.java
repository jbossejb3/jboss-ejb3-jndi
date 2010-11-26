/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
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

package org.jboss.ejb3.jndi.deployers.test.proxy;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Hashtable;

import org.jboss.ejb3.jndi.deployers.proxy.LazyProxyFactory;
import org.jboss.reloaded.naming.service.NameSpaces;

import org.jnp.interfaces.NamingContext;
import org.jnp.server.NamingServer;
import org.jnp.server.SingletonNamingServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test lazy proxy factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LazyProxyTestCase
{
   private static SingletonNamingServer server;
   private static NameSpaces ns;
   protected static InitialContext iniCtx;

   @BeforeClass
   public static void beforeClass() throws NamingException
   {
      server = new SingletonNamingServer();

      ns = new NameSpaces();
      ns.start();

      iniCtx = new InitialContext();
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

   @AfterClass
   public static void afterClass() throws NamingException
   {
      iniCtx.close();

      ns.stop();

      server.destroy();
   }

   @Test
   public void testBasicUsage() throws Exception
   {
      LazyProxyFactory factory = new LazyProxyFactory();
      Context context = createContext();

      // old bind
      context.bind("old-test", new BizIfaceImpl());

      // test interface
      context.bind("new-test", factory.lazyLink(BizIface.class.getName(), "old-test"));
      Object object = context.lookup("new-test");
      Assert.assertTrue(object instanceof BizIface);
      BizIface bi = (BizIface) object;
      Assert.assertEquals(20, bi.calculate(2));

      // test impl
      context.bind("impl-test", factory.lazyLink(BizIfaceImpl.class.getName(), "old-test"));
      object = context.lookup("impl-test");
      Assert.assertTrue(object instanceof BizIfaceImpl);
      BizIfaceImpl bii = (BizIfaceImpl) object;
      Assert.assertEquals(30, bii.calculate(3));
   }
}
