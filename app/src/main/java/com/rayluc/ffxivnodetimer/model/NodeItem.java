package com.rayluc.ffxivnodetimer.model;

import android.databinding.ObservableBoolean;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chris on 7/10/16.
 */
public class NodeItem implements Parcelable {
    public static final Creator<NodeItem> CREATOR = new Creator<NodeItem>() {
        @Override
        public NodeItem createFromParcel(Parcel in) {
            return new NodeItem(in);
        }

        @Override
        public NodeItem[] newArray(int size) {
            return new NodeItem[size];
        }
    };
    public int id;
    public String time;
    public String name;
    public int slot;
    public String zone;
    public String coord;
    public ObservableBoolean timerEnabled = new ObservableBoolean(false);
    public int minuteOffset = 0;

    public NodeItem() {

    }

    public NodeItem(int id, String time, String name, int slot, String zone, String coord) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.slot = slot;
        this.zone = zone;
        this.coord = coord;
    }


    public NodeItem(int id, String time, String name, int slot, String zone, String coord, boolean timerEnabled, int offset) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.slot = slot;
        this.zone = zone;
        this.coord = coord;
        this.timerEnabled = new ObservableBoolean(timerEnabled);
        this.minuteOffset = offset;
    }

    protected NodeItem(Parcel in) {
        id = in.readInt();
        time = in.readString();
        name = in.readString();
        slot = in.readInt();
        zone = in.readString();
        coord = in.readString();
        timerEnabled = in.readParcelable(ObservableBoolean.class.getClassLoader());
        minuteOffset = in.readInt();
    }

    public String getFormattedZoneCoord() {
        return zone + ": " + coord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(time);
        parcel.writeString(name);
        parcel.writeInt(slot);
        parcel.writeString(zone);
        parcel.writeString(coord);
        parcel.writeParcelable(timerEnabled, i);
        parcel.writeInt(minuteOffset);
    }
}
