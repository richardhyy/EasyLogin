package cc.eumc.easylogin.localization;

import cc.eumc.easylogin.EasyLogin;

public class LocalizationManager {
    LanguagePack selectedLanguage;

    public LocalizationManager(EasyLogin instance) {

    }

    public String tr(String originStr) {
        return selectedLanguage.tr(originStr);
    }
}
