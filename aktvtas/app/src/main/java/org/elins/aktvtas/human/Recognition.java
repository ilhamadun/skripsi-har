package org.elins.aktvtas.human;

import android.os.Parcel;
import android.os.Parcelable;

public class Recognition implements Parcelable {
    private final int id;
    private final Float confidence;
    private final HumanActivity humanActivity;

    public Recognition(final int id, final Float confidence) {
        this.id = id;
        this.humanActivity = new HumanActivity(HumanActivity.Id.valueOf(id));
        this.confidence = confidence;
    }

    private Recognition(Parcel in) {
        id = in.readInt();
        confidence = in.readFloat();
        humanActivity = new HumanActivity(HumanActivity.Id.valueOf(id));
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return humanActivity.icon();
    }

    public int getName() {
        return humanActivity.name();
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeFloat(confidence);
    }

    public static final Parcelable.Creator<Recognition> CREATOR =
            new Parcelable.Creator<Recognition>() {
                @Override
                public Recognition createFromParcel(Parcel in) {
                    return new Recognition(in);
                }

                @Override
                public Recognition[] newArray(int size) {
                    return new Recognition[size];
                }
            };
}
