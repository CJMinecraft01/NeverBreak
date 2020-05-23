package cjminecraft.neverbreak;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(name = NeverBreak.NAME, version = NeverBreak.VERSION, modid = NeverBreak.MODID, acceptedMinecraftVersions = NeverBreak.ACCEPTED_MC_VERSIONS, certificateFingerprint = NeverBreak.CERTIFICATE_FINGERPRINT)
public class NeverBreak {
    public static final String MODID = "neverbreak";

    public static final String NAME = "Never Break";

    public static final String VERSION = "${version}";

    public static final String ACCEPTED_MC_VERSIONS = "[1.12,1.12.2]";

    public static final String CERTIFICATE_FINGERPRINT = "${fingerprint}";

    public static final Logger LOGGER = LogManager.getFormatterLogger("neverbreak");
}

