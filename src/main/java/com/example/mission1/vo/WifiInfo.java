package com.example.mission1.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiInfo {
    @SerializedName("X_SWIFI_MGR_NO")
    String no;
    @SerializedName("X_SWIFI_WRDOFC")
    String district;
    @SerializedName("X_SWIFI_MAIN_NM")
    String wifiNm;
    @SerializedName("X_SWIFI_ADRES1")
    String addr1;
    @SerializedName("X_SWIFI_ADRES2")
    String addr2;
    @SerializedName("X_SWIFI_INSTL_FLOOR")
    String floor;
    @SerializedName("X_SWIFI_INSTL_TY")
    String installType;
    @SerializedName("X_SWIFI_INSTL_MBY")
    String institute;
    @SerializedName("X_SWIFI_SVC_SE")
    String serviceType;
    @SerializedName("X_SWIFI_CMCWR")
    String networkType;
    @SerializedName("X_SWIFI_CNSTC_YEAR")
    String year;
    @SerializedName("X_SWIFI_INOUT_DOOR")
    String inOutDoor;
    @SerializedName("X_SWIFI_REMARS3")
    String connEnvir;
    @SerializedName("LAT")
    double lat;
    @SerializedName("LNT")
    double lnt;
    @SerializedName("WORK_DTTM")
    String workDt;
    double distance;

    public WifiInfo() {}
}
