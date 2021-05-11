package fr.redstonneur1256.utils;

import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;

public class UnlockedContent extends UnlockableContent {

    public UnlockedContent(String name) {
        super(null);
    }

    @Override
    public ContentType getContentType() {
        return null;
    }

}
