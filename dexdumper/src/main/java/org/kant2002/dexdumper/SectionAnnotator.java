package org.kant2002.dexdumper;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.ItemType;
import org.jf.dexlib2.dexbacked.raw.MapItem;
import org.jf.dexlib2.util.AlignmentUtils;
import org.jf.dexlib2.util.AnnotatedBytes;

import com.google.common.collect.Maps;

public abstract class SectionAnnotator {
    @Nonnull public final PlainAnnotators annotator;
    @Nonnull public final DexBackedDexFile dexFile;
    public final int itemType;
    public final int sectionOffset;
    public final int itemCount;

    protected Map<Integer, String> itemIdentities = Maps.newHashMap();

    public SectionAnnotator(@Nonnull PlainAnnotators annotator, @Nonnull MapItem mapItem) {
        this.annotator = annotator;
        this.dexFile = annotator.dexFile;
        this.itemType = mapItem.getType();

        if (mapItem.getType() >= ItemType.MAP_LIST) {
            this.sectionOffset = mapItem.getOffset() + dexFile.getBaseDataOffset();
        } else {
            this.sectionOffset = mapItem.getOffset();
        }

        this.itemCount = mapItem.getItemCount();
    }

    @Nonnull public abstract String getItemName();
    protected abstract void annotateItem(@Nonnull AnnotatedBytes out, int itemIndex, @Nullable String itemIdentity);

    /**
     * Write out annotations for this section
     *
     * @param out The AnnotatedBytes object to annotate to
     */
    public void annotateSection(@Nonnull AnnotatedBytes out) {
        out.moveTo(sectionOffset);
        annotateSectionInner(out, itemCount);
    }

    protected int getItemOffset(int itemIndex, int currentOffset) {
        return AlignmentUtils.alignOffset(currentOffset, getItemAlignment());
    }

    protected void annotateSectionInner(@Nonnull AnnotatedBytes out, int itemCount) {
        String itemName = getItemName();
        if (itemCount > 0) {
            out.annotate(0, "");
            out.annotate(0, "-----------------------------");
            out.annotate(0, "%s section", itemName);
            out.annotate(0, "-----------------------------");
            out.annotate(0, "");

            for (int i=0; i<itemCount; i++) {
                out.moveTo(getItemOffset(i, out.getCursor()));

                String itemIdentity = getItemIdentity(out.getCursor());
                if (itemIdentity != null) {
                    out.annotate(0, "[%d] %s: %s", i, itemName, itemIdentity);
                } else {
                    out.annotate(0, "[%d] %s", i, itemName);
                }
                out.indent();
                annotateItem(out, i, itemIdentity);
                out.deindent();
            }
        }
    }

    @Nullable private String getItemIdentity(int itemOffset) {
        return itemIdentities.get(itemOffset);
    }

    public void setItemIdentity(int itemOffset, String identity) {
        itemIdentities.put(itemOffset + dexFile.getBaseDataOffset(), identity);
    }

    public int getItemAlignment() {
        return 1;
    }
}