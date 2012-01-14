/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.internal;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.skalli.gerrit.client.config.ConfigKeyGerrit;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.junit.Test;


@SuppressWarnings("nls")
public class GerritServiceTest {

  private static final String HOST = "some.host";
  private static final String PORT = "12345";
  private static final String USER = "some:user";
  private static final String PRIVATEKEY = "my/key/file";
  private static final String PASSPHRASE = "$ecret";
  private static final String ON_BEHALF_OF = "tiffy";

  @Test
  public void testGetClient() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNotNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientUserIdNull() throws Exception {
    assertNull(new GerritServiceImpl(null).getClient(null));
  }

  @Test
  public void testGetClientConfigurationServiceNull() throws Exception {
    assertNull(new GerritServiceImpl(null).getClient(ON_BEHALF_OF));
  }


  @Test
  public void testGetClientHostNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(null, PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientHostEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay("", PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, null, USER, PRIVATEKEY, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, "", USER, PRIVATEKEY, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortNotNumeric() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, "port", USER, PRIVATEKEY, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientKeyNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, PORT, USER, null, PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientKeyEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, PORT, USER, "", PASSPHRASE);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPassphraseNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, PORT, USER, PRIVATEKEY, null);
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPassphraseEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(HOST, PORT, USER, PRIVATEKEY, "");
    assertNull(new GerritServiceImpl(mockedConfigService).getClient(ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  private ConfigurationService configAndReplay(String host, String port, String user, String privateKey,
      String passphrase) {
    ConfigurationService mockedConfigService = createMock(ConfigurationService.class);

    expect(mockedConfigService.readString(ConfigKeyGerrit.HOST)).andReturn(host);
    expect(mockedConfigService.readString(ConfigKeyGerrit.PORT)).andReturn(port);
    expect(mockedConfigService.readString(ConfigKeyGerrit.USER)).andReturn(user);
    expect(mockedConfigService.readString(ConfigKeyGerrit.PRIVATEKEY)).andReturn(privateKey);
    expect(mockedConfigService.readString(ConfigKeyGerrit.PASSPHRASE)).andReturn(passphrase);

    replay(mockedConfigService);

    return mockedConfigService;
  }
}
