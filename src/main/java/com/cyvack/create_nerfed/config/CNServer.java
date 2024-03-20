package com.cyvack.create_nerfed.config;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.infrastructure.config.CServer;

public class CNServer extends ConfigBase {

    //    //TODO: change to make it area based, instead of depending on how many are in the same system, as this can be exploited
//    public final ConfigGroup infrastructure = group(0, "infrastructure", "test");
    public final ConfigBool groupProcessingEnabled = b(false, "groupProcessing",
            "Determines if Group processing behaviour is enabled.",
            "Group processing is a system that allows machines to more efficiently process items if there are the correct number of machines on the same network",
            "Having less or more than this 'golden' number decreases efficiency, down to normal speed");

    public final CNGroupValues groupValues = nested(0, CNGroupValues::new, "Parameters for group processing");
    public final CNProcessingSpeed processingSpeed = nested(0, CNProcessingSpeed::new, "Parameters for nerfing create processing speed");


    @Override
    public String getName() {
        return "server";
    }

}
