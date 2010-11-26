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

package org.jboss.ejb3.jndi.binder.test.proxy;

import javax.naming.Context;
import javax.naming.Reference;

import org.jboss.ejb3.jndi.binder.impl.AbstractLazyProxyFactory;
import org.jboss.ejb3.jndi.binder.test.common.AbstractNamingTestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test lazy proxy factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LazyProxyTestCase extends AbstractNamingTestCase
{
   @Test
   public void testBasicUsage() throws Exception
   {
      AbstractLazyProxyFactory factory = new DummyLazyProxyFactory();
      Context context = createContext();

      // old bind
      Reference ref = new Reference(BizIfaceImpl.class.getName(), TrackingOF.class.getName(), null);
      context.bind("old-test", ref);

      // test interface
      context.bind("new-test", factory.lazyLinkRef(BizIface.class.getName(), "old-test"));
      Assert.assertFalse(TrackingOF.hit);
      Object object = context.lookup("new-test");
      Assert.assertTrue(object instanceof BizIface);
      BizIface bi = (BizIface) object;
      Assert.assertFalse(TrackingOF.hit);
      Assert.assertEquals(20, bi.calculate(2));
      Assert.assertTrue(TrackingOF.hit);

      TrackingOF.hit = false;

      // test impl
      context.bind("impl-test", factory.lazyLinkRef(BizIfaceImpl.class.getName(), "old-test"));
      Assert.assertFalse(TrackingOF.hit);
      object = context.lookup("impl-test");
      Assert.assertFalse(TrackingOF.hit);
      Assert.assertTrue(object instanceof BizIfaceImpl);
      BizIfaceImpl bii = (BizIfaceImpl) object;
      Assert.assertFalse(TrackingOF.hit);
      Assert.assertEquals(30, bii.calculate(3));
      Assert.assertTrue(TrackingOF.hit);
   }
}
