private void shareMsg(String msgTitle, String msgText,
                          String imgPath) {

        Intent intent = new Intent("android.intent.action.SEND");
        if (StringUtils.isEmpty(imgPath)) {
            intent.setType("text/plain");
        } else {
            File f = new File(imgPath);
            if (f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            }else{
                ToastUtils.showShortToast(mContext,"Í¼Æ¬²»´æÔÚ");
                return;
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, msgTitle));
    }