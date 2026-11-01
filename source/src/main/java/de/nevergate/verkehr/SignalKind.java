package de.nevergate.verkehr;

/** Stable menu IDs and fixed roles for the physical signal variants. */
public enum SignalKind {
    VEHICLE("signal", "Fahrzeugampel", -1, 0),
    LEFT("signal_left", "Linksabbieger", 1, 5),
    RIGHT("signal_right", "Rechtsabbieger", 2, 6),
    STRAIGHT("signal_straight", "Geradeaus-Pfeil", 0, 7),
    STRAIGHT_LEFT("signal_straight_left", "Geradeaus / Links", 3, 8),
    STRAIGHT_RIGHT("signal_straight_right", "Geradeaus / Rechts", 4, 9),
    LEFT_RIGHT("signal_left_right", "Links / Rechts", 5, 10),
    ALL("signal_all", "Alle Richtungen", 6, 11),
    BICYCLE("bicycle_signal", "Fahrradampel", 0, 12),
    WARNING("warning_signal", "Gelbes Blinklicht", 0, 13),
    MOBILE("mobile_signal", "Mobile Baustellenampel", -1, 14);
    public final String id,label;
    public final int movement,menuType;
    SignalKind(String id,String label,int movement,int menuType) { this.id=id;this.label=label;this.movement=movement;this.menuType=menuType; }
    public static SignalKind forMenu(int type) { for(var kind:values())if(kind.menuType==type)return kind;return null; }
}
