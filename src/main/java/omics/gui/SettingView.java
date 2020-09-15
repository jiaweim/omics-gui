package omics.gui;

/**
 * View of settings
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 5:04 PM
 */
public interface SettingView
{
    /**
     * Get values from view, and update the setting values
     *
     * @param setting {@link Setting} to update
     */
    void updateSetting(Setting setting);

    /**
     * update the view with values
     *
     * @param setting settings
     */
    void updateView(Setting setting);
}
