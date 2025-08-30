package org.kant2002.dexdumper;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jf.dexlib2.dexbacked.CDexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.AnnotationDirectoryItem;
import org.jf.dexlib2.dexbacked.raw.AnnotationItem;
import org.jf.dexlib2.dexbacked.raw.AnnotationSetItem;
import org.jf.dexlib2.dexbacked.raw.AnnotationSetRefList;
import org.jf.dexlib2.dexbacked.raw.CallSiteIdItem;
import org.jf.dexlib2.dexbacked.raw.CdexDebugOffsetTable;
import org.jf.dexlib2.dexbacked.raw.CdexHeaderItem;
import org.jf.dexlib2.dexbacked.raw.ClassDataItem;
import org.jf.dexlib2.dexbacked.raw.ClassDefItem;
import org.jf.dexlib2.dexbacked.raw.CodeItem;
import org.jf.dexlib2.dexbacked.raw.DebugInfoItem;
import org.jf.dexlib2.dexbacked.raw.EncodedArrayItem;
import org.jf.dexlib2.dexbacked.raw.FieldIdItem;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.dexbacked.raw.HiddenApiClassDataItem;
import org.jf.dexlib2.dexbacked.raw.ItemType;
import org.jf.dexlib2.dexbacked.raw.MapItem;
import org.jf.dexlib2.dexbacked.raw.MethodHandleItem;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;
import org.jf.dexlib2.dexbacked.raw.StringDataItem;
import org.jf.dexlib2.dexbacked.raw.StringIdItem;
import org.jf.dexlib2.dexbacked.raw.TypeIdItem;
import org.jf.dexlib2.dexbacked.raw.TypeListItem;
import org.jf.dexlib2.dexbacked.raw.util.DexAnnotator;
import org.jf.dexlib2.util.AnnotatedBytes;
import org.jf.util.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class PlainAnnotators extends AnnotatedBytes {
    @Nonnull public final DexBackedDexFile dexFile;

    private final Map<Integer, SectionAnnotator> annotators = Maps.newHashMap();
    private static final Map<Integer, Integer> sectionAnnotationOrder = Maps.newHashMap();

    static {
        int[] sectionOrder = new int[] {
                ItemType.MAP_LIST,

                ItemType.HEADER_ITEM,
                ItemType.STRING_ID_ITEM,
                ItemType.TYPE_ID_ITEM,
                ItemType.PROTO_ID_ITEM,
                ItemType.FIELD_ID_ITEM,
                ItemType.METHOD_ID_ITEM,
                ItemType.CALL_SITE_ID_ITEM,
                ItemType.METHOD_HANDLE_ITEM,

                // these need to be ordered like this, so the item identities can be propagated
                ItemType.CLASS_DEF_ITEM,
                ItemType.CLASS_DATA_ITEM,
                ItemType.CODE_ITEM,
                ItemType.DEBUG_INFO_ITEM,

                ItemType.TYPE_LIST,
                ItemType.ANNOTATION_SET_REF_LIST,
                ItemType.ANNOTATION_SET_ITEM,
                ItemType.STRING_DATA_ITEM,
                ItemType.ANNOTATION_ITEM,
                ItemType.ENCODED_ARRAY_ITEM,
                ItemType.ANNOTATION_DIRECTORY_ITEM,

                ItemType.HIDDENAPI_CLASS_DATA_ITEM
        };

        for (int i=0; i<sectionOrder.length; i++) {
            sectionAnnotationOrder.put(sectionOrder[i], i);
        }
    }

    public PlainAnnotators(@Nonnull DexBackedDexFile dexFile, int width) {
        super(width);
        this.dexFile = dexFile;

        for (MapItem mapItem: dexFile.getMapItems()) {
            switch (mapItem.getType()) {
                case ItemType.HEADER_ITEM:
                    annotators.put(mapItem.getType(), makeHeaderAnnotator(this, mapItem));
                    break;
                case ItemType.STRING_ID_ITEM:
                case ItemType.TYPE_ID_ITEM:
                case ItemType.PROTO_ID_ITEM:
                case ItemType.FIELD_ID_ITEM:
                case ItemType.METHOD_ID_ITEM:
                case ItemType.CLASS_DEF_ITEM:
                case ItemType.MAP_LIST:
                case ItemType.TYPE_LIST:
                case ItemType.ANNOTATION_SET_ITEM:
                case ItemType.ANNOTATION_SET_REF_LIST:
                case ItemType.CLASS_DATA_ITEM:
                case ItemType.CODE_ITEM:
                case ItemType.STRING_DATA_ITEM:
                case ItemType.DEBUG_INFO_ITEM:
                case ItemType.ANNOTATION_ITEM:
                case ItemType.ENCODED_ARRAY_ITEM:
                case ItemType.ANNOTATION_DIRECTORY_ITEM:
                case ItemType.CALL_SITE_ID_ITEM:
                case ItemType.METHOD_HANDLE_ITEM:
                case ItemType.HIDDENAPI_CLASS_DATA_ITEM:
                    break;
                // case ItemType.STRING_ID_ITEM:
                //     annotators.put(mapItem.getType(), StringIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.TYPE_ID_ITEM:
                //     annotators.put(mapItem.getType(), TypeIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.PROTO_ID_ITEM:
                //     annotators.put(mapItem.getType(), ProtoIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.FIELD_ID_ITEM:
                //     annotators.put(mapItem.getType(), FieldIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.METHOD_ID_ITEM:
                //     annotators.put(mapItem.getType(), MethodIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.CLASS_DEF_ITEM:
                //     annotators.put(mapItem.getType(), ClassDefItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.MAP_LIST:
                //     annotators.put(mapItem.getType(), MapItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.TYPE_LIST:
                //     annotators.put(mapItem.getType(), TypeListItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.ANNOTATION_SET_REF_LIST:
                //     annotators.put(mapItem.getType(), AnnotationSetRefList.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.ANNOTATION_SET_ITEM:
                //     annotators.put(mapItem.getType(), AnnotationSetItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.CLASS_DATA_ITEM:
                //     annotators.put(mapItem.getType(), ClassDataItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.CODE_ITEM:
                //     annotators.put(mapItem.getType(), CodeItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.STRING_DATA_ITEM:
                //     annotators.put(mapItem.getType(), StringDataItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.DEBUG_INFO_ITEM:
                //     annotators.put(mapItem.getType(), DebugInfoItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.ANNOTATION_ITEM:
                //     annotators.put(mapItem.getType(), AnnotationItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.ENCODED_ARRAY_ITEM:
                //     annotators.put(mapItem.getType(), EncodedArrayItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.ANNOTATION_DIRECTORY_ITEM:
                //     annotators.put(mapItem.getType(), AnnotationDirectoryItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.CALL_SITE_ID_ITEM:
                //     annotators.put(mapItem.getType(), CallSiteIdItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.METHOD_HANDLE_ITEM:
                //     annotators.put(mapItem.getType(), MethodHandleItem.makeAnnotator(this, mapItem));
                //     break;
                // case ItemType.HIDDENAPI_CLASS_DATA_ITEM:
                //     annotators.put(mapItem.getType(), HiddenApiClassDataItem.makeAnnotator(this, mapItem));
                //     break;
                default:
                    throw new RuntimeException(String.format("Unrecognized item type: 0x%x", mapItem.getType()));
            }
        }
    }

    public void writeAnnotations(Writer out) throws IOException {
        List<MapItem> mapItems = dexFile.getMapItems();
        // sort the map items based on the order defined by sectionAnnotationOrder
        Ordering<MapItem> ordering = Ordering.from(new Comparator<MapItem>() {
            @Override public int compare(MapItem o1, MapItem o2) {
                return Ints.compare(sectionAnnotationOrder.get(o1.getType()), sectionAnnotationOrder.get(o2.getType()));
            }
        });

        mapItems = ordering.immutableSortedCopy(mapItems);

        try {
            // Need to annotate the debug info offset table first, to propagate the debug info identities
            // if (dexFile instanceof CDexBackedDexFile) {
            //     moveTo(dexFile.getBaseDataOffset() + ((CDexBackedDexFile) dexFile).getDebugInfoOffsetsPos());
            //     CdexDebugOffsetTable.annotate(this, dexFile.getBuffer());
            // }

            for (MapItem mapItem: mapItems) {
                //try {
                    SectionAnnotator annotator = annotators.get(mapItem.getType());
                    if (annotator != null) {
                        annotator.annotateSection(this);
                    }
                // } catch (Exception ex) {
                //     System.err.println(String.format("There was an error while dumping the %s section",
                //             ItemType.getItemTypeName(mapItem.getType())));
                //     ex.printStackTrace(System.err);
                // }
            }
        } finally {
            // We don't want dump what's left in the file.
            int baseOffset = dexFile.getBuffer().getBaseOffset();
            writeAnnotations(out, Arrays.copyOfRange(dexFile.getBuffer().getBuf(), baseOffset, baseOffset + HeaderItem.ITEM_SIZE), baseOffset);
        }
    }    

    @Nonnull
    public static SectionAnnotator makeHeaderAnnotator(@Nonnull PlainAnnotators annotator, @Nonnull MapItem mapItem) {
        return new SectionAnnotator(annotator, mapItem) {
            @Nonnull @Override public String getItemName() {
                return "header_item";
            }

            @Override
            protected void annotateItem(@Nonnull AnnotatedBytes out, int itemIndex, @Nullable String itemIdentity) {
                int startOffset = out.getCursor();
                int headerSize;

                StringBuilder magicBuilder = new StringBuilder();
                for (int i=0; i<8; i++) {
                    magicBuilder.append((char)dexFile.getBuffer().readUbyte(startOffset + i));
                }

                out.annotate(8, "magic: %s", StringUtils.escapeString(magicBuilder.toString()));
                out.annotate(4, "checksum");
                out.annotate(20, "signature");
                out.annotate(4, "file_size: %d", dexFile.getBuffer().readInt(out.getCursor()));

                headerSize = dexFile.getBuffer().readInt(out.getCursor());
                out.annotate(4, "header_size: %d", headerSize);

                int endianTag = dexFile.getBuffer().readInt(out.getCursor());
                out.annotate(4, "endian_tag: 0x%x (%s)", endianTag, getEndianText(endianTag));

                out.annotate(4, "link_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "link_offset: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "map_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "string_ids_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "string_ids_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "type_ids_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "type_ids_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "proto_ids_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "proto_ids_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "field_ids_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "field_ids_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "method_ids_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "method_ids_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "class_defs_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "class_defs_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                out.annotate(4, "data_size: %d", dexFile.getBuffer().readInt(out.getCursor()));
                out.annotate(4, "data_off: 0x%x", dexFile.getBuffer().readInt(out.getCursor()));

                if (annotator.dexFile instanceof CDexBackedDexFile) {
                    CdexHeaderItem.annotateCdexHeaderFields(out, dexFile.getBuffer());
                }

                if (headerSize > HeaderItem.ITEM_SIZE) {
                    out.annotateTo(headerSize, "header padding");
                }
            }
        };
    }

    private static String getEndianText(int endianTag) {
        if (endianTag == HeaderItem.LITTLE_ENDIAN_TAG) {
            return "Little Endian";
        }
        if (endianTag == HeaderItem.BIG_ENDIAN_TAG) {
            return "Big Endian";
        }
        return "Invalid";
    }

    @Nullable
    public SectionAnnotator getAnnotator(int itemType) {
        return annotators.get(itemType);
    }
}
