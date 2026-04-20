package hall_wedding;

public class Theme {

    // ─── Palette ─────────────────────────────────────────
    public static final String BG_DARK      = "#F9F4EC";
    public static final String BG_CARD      = "#FFFDF8";
    public static final String BG_INPUT     = "#F3E9DA";
    public static final String GOLD         = "#B8893B";
    public static final String GOLD_LIGHT   = "#D3A85E";
    public static final String ROSE         = "#C89286";
    public static final String ROSE_LIGHT   = "#E8B9AA";
    public static final String CREAM        = "#2F241B";
    public static final String CREAM_DIM    = "#7F6A55";
    public static final String WHITE        = "#FFFFFF";
    public static final String ERROR        = "#E05555";
    public static final String SUCCESS      = "#6DBF82";
    private static final String FLORAL_BG_PATH = "/viewIcon/photo_2026-04-21_00-01-45.jpg";

    // ─── Styles ──────────────────────────────────────────
    public static String appBackgroundStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #FFF9F0 0%, #F8ECDC 55%, #F2E4D3 100%);";
    }

    public static String pageBackgroundStyle() {
        String bgUrl = Theme.class.getResource(FLORAL_BG_PATH) != null
                ? Theme.class.getResource(FLORAL_BG_PATH).toExternalForm()
                : "";
        return "-fx-background-color:#F9F3EA;" +
               "-fx-background-image:url('" + bgUrl + "');" +
               "-fx-background-size:cover;" +
               "-fx-background-position:center center;" +
               "-fx-background-repeat:no-repeat;";
    }

    public static String frostedPanelStyle() {
        return "-fx-background-color: rgba(255,253,248,0.84);" +
               "-fx-background-radius:14;" +
               "-fx-border-color: rgba(184,137,59,0.25);" +
               "-fx-border-radius:14;" +
               "-fx-border-width:1;";
    }

    public static String cardStyle() {
        return "-fx-background-color: rgba(255,253,248,0.95);" +
               "-fx-background-radius:18;" +
               "-fx-border-color: rgba(184,137,59,0.38);" +
               "-fx-border-radius:18;" +
               "-fx-border-width:1.1;" +
               "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.16), 20, 0.2, 0, 6);";
    }

    public static String inputStyle() {
        return "-fx-background-color:" + BG_INPUT + ";" +
               "-fx-text-fill:" + CREAM + ";" +
               "-fx-prompt-text-fill:" + CREAM_DIM + ";" +
               "-fx-padding:10 14;" +
               "-fx-background-radius:10;" +
               "-fx-border-color: rgba(184,137,59,0.22);" +
               "-fx-border-radius:10;" +
               "-fx-font-size:13px;";
    }

    public static String goldButtonStyle() {
        return "-fx-background-color:" + GOLD + ";" +
               "-fx-text-fill:#FFFDF8;" +
               "-fx-font-weight:bold;" +
               "-fx-font-size:14px;" +
               "-fx-padding:10 30;" +
               "-fx-background-radius:20;" +
               "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.18), 12, 0.2, 0, 3);" +
               "-fx-cursor:hand;";
    }

    public static String roseButtonStyle() {
        return "-fx-background-color:" + ROSE + ";" +
               "-fx-text-fill:" + WHITE + ";" +
               "-fx-font-weight:bold;" +
               "-fx-font-size:13px;" +
               "-fx-padding:8 20;" +
               "-fx-background-radius:10;" +
               "-fx-cursor:hand;";
    }

    public static String dangerButtonStyle() {
        return "-fx-background-color:#8B2020;" +
               "-fx-text-fill:" + WHITE + ";" +
               "-fx-font-weight:bold;" +
               "-fx-font-size:13px;" +
               "-fx-padding:8 20;" +
               "-fx-background-radius:10;" +
               "-fx-cursor:hand;";
    }

    public static String navButtonStyle() {
        return "-fx-background-color:transparent;" +
               "-fx-text-fill:" + CREAM_DIM + ";" +
               "-fx-font-size:14px;" +
               "-fx-font-family:'Georgia';" +
               "-fx-alignment:CENTER_LEFT;" +
               "-fx-padding:10 16;" +
               "-fx-background-radius:10;" +
               "-fx-cursor:hand;";
    }

    public static String navButtonActiveStyle() {
        return "-fx-background-color:" + BG_INPUT + ";" +
               "-fx-text-fill:" + GOLD + ";" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-font-family:'Georgia';" +
               "-fx-alignment:CENTER_LEFT;" +
               "-fx-padding:10 16;" +
               "-fx-background-radius:10;" +
               "-fx-cursor:hand;";
    }

    public static String titleStyle(int size) {
        return "-fx-font-size:" + size + "px;" +
               "-fx-text-fill:" + GOLD + ";" +
               "-fx-font-family:'Palatino Linotype', 'Georgia';" +
               "-fx-font-weight:bold;";
    }

    public static String subtitleStyle() {
        return "-fx-font-size:13px;" +
               "-fx-text-fill:" + CREAM_DIM + ";" +
               "-fx-font-family:'Georgia';";
    }

    public static String tableStyle() {
        return "-fx-background-color:" + BG_CARD + ";" +
               "-fx-border-color:" + GOLD + ";" +
               "-fx-border-width:1;" +
               "-fx-border-radius:10;" +
               "-fx-background-radius:10;" +
               "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.1), 10, 0.15, 0, 2);";
    }
}