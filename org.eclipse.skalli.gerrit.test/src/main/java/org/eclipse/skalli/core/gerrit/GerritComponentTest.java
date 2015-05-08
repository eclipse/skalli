/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.gerrit;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.gerrit.GerritServerConfig;
import org.eclipse.skalli.services.gerrit.GerritServersConfig;
import org.junit.Test;


@SuppressWarnings("nls")
public class GerritComponentTest {

  private static final String ID = "someId";
  private static final String HOST = "some.host";
  private static final String PORT = "12345";
  private static final String USER = "some:user";
  private static final String PRIVATEKEY = "my/key/file";
  private static final String PASSPHRASE = "$ecret";
  private static final String ON_BEHALF_OF = "tiffy";

  private GerritComponent getServiceImpl(ConfigurationService configService) {
      GerritComponent serviceImpl = new GerritComponent();
      serviceImpl.bindConfigurationService(configService);
      return serviceImpl;
  }

  @Test
  public void testGetClient() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNotNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientUserIdNull() throws Exception {
    assertNull(getServiceImpl(null).getClient(ID, null));
  }

  @Test
  public void testGetClientConfigurationServiceNull() throws Exception {
    assertNull(getServiceImpl(null).getClient(ID, ON_BEHALF_OF));
  }


  @Test
  public void testGetClientHostNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, null, PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientHostEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, "", PORT, USER, PRIVATEKEY, PASSPHRASE);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, null, USER, PRIVATEKEY, PASSPHRASE);
    assertNotNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF)); // client with default port 29418!
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, "", USER, PRIVATEKEY, PASSPHRASE);
    assertNotNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF)); // client with default port 29418!
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPortNotNumeric() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, "port", USER, PRIVATEKEY, PASSPHRASE);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));  // no fallback if port is invalid!
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientKeyNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, null, PASSPHRASE);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientKeyEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, "", PASSPHRASE);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPassphraseNull() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, PRIVATEKEY, null);
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientPassphraseEmpty() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, PRIVATEKEY, "");
    assertNull(getServiceImpl(mockedConfigService).getClient(ID, ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  @Test
  public void testGetClientUnknownId() throws Exception {
    ConfigurationService mockedConfigService = configAndReplay(ID, HOST, PORT, USER, PRIVATEKEY, "");
    assertNull(getServiceImpl(mockedConfigService).getClient("foo", ON_BEHALF_OF));
    verify(mockedConfigService);
  }

  private ConfigurationService configAndReplay(String id, String host, String port, String user, String privateKey,
      String passphrase) {
    GerritServerConfig gerritConfig = new GerritServerConfig();
    gerritConfig.setId(id);
    gerritConfig.setHost(host);
    gerritConfig.setPort(port);
    gerritConfig.setUser(user);
    gerritConfig.setPrivateKey(privateKey);
    gerritConfig.setPassphrase(passphrase);
    GerritServersConfig serversConfig = new GerritServersConfig();
    serversConfig.addServer(gerritConfig);
    ConfigurationService mockedConfigService = createMock(ConfigurationService.class);

    expect(mockedConfigService.readConfiguration(GerritServersConfig.class)).andReturn(serversConfig);

    replay(mockedConfigService);
    return mockedConfigService;
  }
}
