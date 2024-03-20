package com.cyvack.create_nerfed.mixinterfaces;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

public interface GroupAmountGetterBE {

    static <T extends KineticBlockEntity> int get(T be) {
        if (be instanceof GroupAmountGetterBE getter) {
            return getter.getKineticCount();
        }

        return 0;
    }

    int getKineticCount();

}
