//package top.lichuanjiu.sftpclient.Tools;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//public class FileClassifier {
//    private Map<String, Integer> fileTypeMap;
//
//    public FileClassifier() {
////        fileTypeMap = new HashMap<>();
////        // 添加不同文件类型和对应的图片资源ID
////        fileTypeMap.put("txt", R.drawable.ic_file_txt);
////        fileTypeMap.put("pdf", R.drawable.ic_file_pdf);
////        fileTypeMap.put("doc", R.drawable.ic_file_doc);
////        fileTypeMap.put("docx", R.drawable.ic_file_doc);
////        fileTypeMap.put("xls", R.drawable.ic_file_xls);
////        fileTypeMap.put("xlsx", R.drawable.ic_file_xls);
////        fileTypeMap.put("ppt", R.drawable.ic_file_ppt);
////        fileTypeMap.put("pptx", R.drawable.ic_file_ppt);
////        fileTypeMap.put("jpg", R.drawable.ic_file_jpg);
////        fileTypeMap.put("jpeg", R.drawable.ic_file_jpg);
////        fileTypeMap.put("png", R.drawable.ic_file_png);
////        fileTypeMap.put("gif", R.drawable.ic_file_gif);
////        fileTypeMap.put("mp3", R.drawable.ic_file_mp3);
////        fileTypeMap.put("wav", R.drawable.ic_file_wav);
////        fileTypeMap.put("mp4", R.drawable.ic_file_mp4);
////        fileTypeMap.put("avi", R.drawable.ic_file_avi);
////        fileTypeMap.put("zip", R.drawable.ic_file_zip);
////        fileTypeMap.put("rar", R.drawable.ic_file_rar);
////        fileTypeMap.put("exe", R.drawable.ic_file_exe);
//// 添加更多文件类型...
//
//        // 添加更多文件类型...
//    }
//
//    public int getFileIconResourceId(File file) {
//        // 获取文件扩展名
//        String extension = getFileExtension(file);
//        // 查找对应的图片资源ID
//        Integer resourceId = fileTypeMap.get(extension);
//        // 如果找不到对应的图片资源ID，则返回默认图片资源ID
//        if (resourceId == null) {
//            resourceId = R.drawable.ic_file_default;
//        }
//        return resourceId;
//    }
//
//    private String getFileExtension(File file) {
//        String fileName = file.getName();
//        int dotIndex = fileName.lastIndexOf(".");
//        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
//            return fileName.substring(dotIndex + 1).toLowerCase();
//        }
//        return "";
//    }
//}
