package com.rayluc.ffxivnodetimer.model;

import com.rayluc.ffxivnodetimer.data.ProviderContracts;

/**
 * Created by chris on 7/10/16.
 */
public class NodeItem {
    public int id;
    public String time;
    public String name;
    public int slot;
    public String zone;
    public String coord;
    public
    @ProviderContracts.Disciples
    int disciple;

    public NodeItem() {

    }

    public NodeItem(int id, String time, String name, int slot, String zone, String coord, int disciple) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.slot = slot;
        this.zone = zone;
        this.coord = coord;
        this.disciple = disciple;
    }
}
