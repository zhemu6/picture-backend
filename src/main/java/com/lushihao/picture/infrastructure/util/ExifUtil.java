package com.lushihao.picture.infrastructure.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lushihao.picture.infrastructure.manager.upload.model.dto.file.UploadPictureResult;

import java.io.File;

public class ExifUtil {

    public static void fillExifInfo(File file, UploadPictureResult result) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            // 主EXIF信息（相机、镜头等）
            ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0 != null) {
                result.setCameraModel(exifIFD0.getString(ExifIFD0Directory.TAG_MODEL));
            }

            // 拍摄参数信息
            ExifSubIFDDirectory exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSubIFD != null) {
                result.setFNumber(exifSubIFD.getDoubleObject(ExifSubIFDDirectory.TAG_FNUMBER));
                result.setIso(exifSubIFD.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                result.setExposureTime(exifSubIFD.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                result.setFocalLength(exifSubIFD.getDoubleObject(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
                result.setTakenTime(exifSubIFD.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                result.setLensModel(exifSubIFD.getString(ExifSubIFDDirectory.TAG_LENS_MODEL));
            }

        } catch (Exception e) {
            // 若图片没有EXIF信息或解析失败，不抛出异常，仅记录
            System.err.println("无法解析 EXIF 信息：" + e.getMessage());
        }
    }
}
