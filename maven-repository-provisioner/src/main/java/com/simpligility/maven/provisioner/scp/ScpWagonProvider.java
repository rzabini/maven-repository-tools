package com.simpligility.maven.provisioner.scp;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.ssh.jsch.ScpWagon;
import org.apache.maven.wagon.providers.ssh.knownhost.FileKnownHostsProvider;
import org.eclipse.aether.transport.wagon.WagonProvider;

import java.io.File;

public class ScpWagonProvider implements WagonProvider {

    public Wagon lookup(String roleHint)
            throws Exception {
        ScpWagon scpWagon = new ScpWagon();
        scpWagon.setKnownHostsProvider(new FileKnownHostsProvider(new File(
                new File(System.getProperty("user.home"), ".ssh"), "known_hosts")));
        return scpWagon;
    }

    @Override
    public void release(Wagon wagon) {

    }

}
