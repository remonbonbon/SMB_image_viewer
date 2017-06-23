package cx.myhome.thisis.smbimageviewer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

class AsyncSmbAccess extends AsyncTask<String, Void, List<SmbFile>> {

    private Activity mainActivity;

    AsyncSmbAccess(Activity activity) {
        this.mainActivity = activity;
    }

    private class SmbFileListAdapter extends ArrayAdapter<SmbFile> {

        private Context context;
        private int layoutId;
        private LayoutInflater inflater;
        private List<SmbFile> items;

        private SmbFileListAdapter(Context context, int layoutId, List<SmbFile> items) {
            super(context, layoutId, items);
            this.context = context;
            this.layoutId = layoutId;
            this.items = items;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            SmbFile item = items.get(position);

            class ViewHolder {
                private ImageView imageView;
            }

            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(layoutId, parent, false);
                // ViewHolder を生成
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            Glide.with(context)
                    .load(item.getPath())
                    .asBitmap()
                    .error(R.drawable.ic_load_failed)
                    .placeholder(R.drawable.ic_loading)
                    .centerCrop()
                    .thumbnail(0.1f)
                    .into(holder.imageView);

            return convertView;
        }
    }

    @Override
    protected List<SmbFile> doInBackground(String... params) {
        String uri = params[0];
        List<SmbFile> list = new ArrayList<>();
        try {
            SmbFile dirFrom = new SmbFile(uri);
            SmbFile[] files = dirFrom.listFiles();

            Collections.addAll(list, files);
        } catch (SmbException | MalformedURLException e) {
            e.printStackTrace();
        }

        Collections.sort(list, new Comparator<SmbFile>() {
            private Pattern _pattern = Pattern.compile("\\d{1,18}");

            @Override
            public int compare(SmbFile o1, SmbFile o2) {
                long d1 = o1.getDate();
                long d2 = o2.getDate();
                if (d1 < d2) return 1;
                if (d1 > d2) return -1;

                String s1 = o1.getName();
                String s2 = o2.getName();

                int res = compareString(s1, s2);
                // compareStringでは大文字小文字の区別をしないので、
                // そちらの結果が「等しい」ならここで大文字小文字の区別を含めた比較を改めて行う。
                return res == 0 ? s1.compareTo(s2) : res;
            }

            private int compareString(final String o1, final String o2) {
                final int len1 = o1.length();
                final int len2 = o2.length();
                Matcher matcher1 = _pattern.matcher(o1);
                Matcher matcher2 = _pattern.matcher(o2);
                int start1 = 0, start2 = 0, end1 = 0, end2 = 0, index1 = 0, index2 = 0;
                String strD1 = null;
                String strD2 = null;
                long d1 = 0;
                long d2 = 0;

                while (true) {
                    boolean find1 = matcher1.find();
                    boolean find2 = matcher2.find();

                    if (find1) {
                        start1 = matcher1.start();
                        end1 = matcher1.end();
                        strD1 = o1.substring(index1, start1);
                        d1 = Long.parseLong(o1.substring(start1, end1));
                        index1 = end1;
                    } else {
                        strD1 = o1.substring(index1);
                        index1 = len1;
                    }

                    if (find2) {
                        start2 = matcher2.start();
                        end2 = matcher2.end();
                        strD2 = o2.substring(index2, start2);
                        d2 = Long.parseLong(o2.substring(start2, end2));
                        index2 = end2;
                    } else {
                        strD2 = o2.substring(index2);
                        index2 = len2;
                    }

                    if (find1 || find2) {// どちらかに数字が含まれる
                        int eq = strD1.compareToIgnoreCase(strD2);
                        if (eq != 0) {
                            return eq;
                        }

                        if (find1 && (!find2)) {// o1が(文字列+数字)でo2が(文字列)のみ → o2の方が先
                            return 1;
                        } else if ((!find1) && find2) {// o1が(文字列)のみでo2が(文字列+数字) → o1の方が先
                            return -1;
                        } else if (d1 != d2) {// 数字で比較
                            return d1 < d2 ? -1 : 1;
                        } else {// 数字が同じなら、数字の文字数で比較。それも同じなら次の比較へ
                            eq = (end1 - start1) - (end2 - start2);
                            if (eq != 0) {
                                return eq;
                            }
                        }
                    } else {// 両方文字列のみ
                        return strD1.compareToIgnoreCase(strD2);
                    }

                    if (index1 == len1) {
                        return index2 == len2 ? 0 : -1;
                    } else if (index2 == len2) {
                        return 1;
                    }
                }
            }
        });
        return list;
    }

    @Override
    protected void onPostExecute(List<SmbFile> list) {
        SmbFileListAdapter adapter = new SmbFileListAdapter(this.mainActivity, R.layout.filelist_row, list);

        GridView gridView = (GridView)mainActivity.findViewById(R.id.imageGridView);
        gridView.setAdapter(adapter);
    }
}
