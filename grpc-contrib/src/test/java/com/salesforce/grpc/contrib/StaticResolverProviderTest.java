/*
 *  Copyright (c) 2017, salesforce.com, inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license. 
 *  For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.grpc.contrib;

import io.grpc.*;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class StaticResolverProviderTest {
    private final InetSocketAddress staticAddress = new InetSocketAddress("localhost", 55555);

    @Test
    public void ProviderShouldProvide() {
        StaticResolverProvider provider = new StaticResolverProvider(staticAddress);
        NameResolver resolver = provider.newNameResolver(URI.create("mesh://some.service"), Attributes.EMPTY);

        assertThat(resolver).isNotNull();
    }

    @Test
    public void ResolverShouldHaveCorrectAuthority() {
        StaticResolverProvider provider = new StaticResolverProvider(staticAddress);
        NameResolver resolver = provider.newNameResolver(URI.create("mesh://some.service"), Attributes.EMPTY);

        assertThat(resolver.getServiceAuthority()).isEqualTo("some.service");
    }

    @Test
    public void ResolverShouldResolve() {
        StaticResolverProvider provider = new StaticResolverProvider(staticAddress);
        NameResolver resolver = provider.newNameResolver(URI.create("mesh://some.service"), Attributes.EMPTY);

        AtomicBoolean isResolved = new AtomicBoolean();

        resolver.start(new NameResolver.Listener() {
            @Override
            @Deprecated
            public void onUpdate(List<ResolvedServerInfoGroup> servers, Attributes attributes) {
                throw new NotImplementedException();
            }

            @Override
            public void onAddresses(List<EquivalentAddressGroup> servers, Attributes attributes) {
                isResolved.set(true);
                assertThat(attributes).isEqualTo(Attributes.EMPTY);

                SocketAddress resolved = servers.get(0).getAddresses().get(0);
                InetSocketAddress address = (InetSocketAddress)resolved;
                assertThat(address).isEqualTo(staticAddress);
            }

            @Override
            public void onError(Status error) {
                fail("Unexpected resolver error");
            }
        });

        assertThat(isResolved.get()).isTrue();
    }
}
