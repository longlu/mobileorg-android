package com.matburt.mobileorg.ui;

import java.util.ArrayList;
import java.util.List;

import com.matburt.mobileorg.R;
import com.matburt.mobileorg.service.DataController;
import com.matburt.mobileorg.service.NoteNG;
import com.matburt.mobileorg.ui.theme.Default;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class OutlineViewerAdapter implements ListAdapter {

	Integer id = null;
	Integer selected = null;
	Integer clicked = null;
	Default theme = null;

	public OutlineViewerAdapter() {
		theme = new Default();
	}

	private static final String TAG = "OutlineView";
	List<NoteNG> data = new ArrayList<NoteNG>();
	DataSetObserver observer = null;
	DataController controller = null;
	private boolean wide = false;

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public NoteNG getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).id;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	public static void addSpan(SpannableStringBuilder buffer, String text,
			Object span) {
		int start = buffer.length();
		int end = start + text.length();
		buffer.append(text);
		if (null != span) {
			buffer.setSpan(span, start, end, 0);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.outline_viewer_item,
					parent, false);
		}
		NoteNG note = getItem(position);
//		boolean isselected = selected == note.id;
//		boolean isclicked = clicked == note.id;
		TextView title = (TextView) convertView
				.findViewById(R.id.outline_viewer_item_text);
//		Log.i(TAG, "getView: "+note.title+", "+isselected+
//				", "+convertView.isFocused()+
//				", "+title.isFocused()+
//				", "+parent.isFocused());
//		convertView.setBackgroundColor(isselected 
//				? theme.c2Green 
//				: isclicked
//					? theme.c7White
//					: theme.c0Black);
		SpannableStringBuilder sb = new SpannableStringBuilder();
		String indent = "";
		if (note.indent > 0) {
			// if (selected == note.id && note.isExpandable()) {
			// sb.append(note.expanded? 'v': '>');
			// } else {
			sb.append(' ');
			// }
			for (int i = 1; i < note.indent; i++) {
				sb.append(' ');
			}
			indent = new String(sb.toString());
		}
		if (wide && null != note.before) {
			addSpan(sb, note.before, new ForegroundColorSpan(theme.c3Yellow));
		}
		if (null != note.todo) {
			addSpan(sb, note.todo + ' ', 
					new ForegroundColorSpan(theme.c1Red));
			// sb.append(note.todo+' ');
		}
		if (null != note.priority) {
			addSpan(sb, "[#" + note.priority + "] ", new ForegroundColorSpan(
					theme.c2Green));
		}
		if (NoteNG.TYPE_SUBLIST.equals(note.type)) {
			String[] lines = note.raw.split("\\n");
			for (int i = 0; i < lines.length; i++) {
				if (i == 0) {
					addSpan(sb, note.before+' ', null);
//					if (note.expanded == NoteNG.EXPAND_ONE) {
//						break;
//					}
				} else {
					addSpan(sb, '\n'+indent, null);
				}
				addSpan(sb, lines[i], null);
			}
		} else if (NoteNG.TYPE_AGENDA.equals(note.type)) {
			addSpan(sb, note.title, new ForegroundColorSpan(
					theme.ccLBlue));
		} else {
			addSpan(sb, note.title, new ForegroundColorSpan(
					theme.cfLWhite));
		}
		title.setText(sb, BufferType.SPANNABLE);
		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		Log.i(TAG, "Register observer: " + observer);
		this.observer = observer;
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.observer = null;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	public void setController(Integer id, DataController controller) {
		if (null == this.controller) {
			this.id = id;
			this.controller = controller;
			reload();
		}
	}

	public void reload() {
		List<NoteNG> _list = controller.getData(id);
		if (null != _list) {
			data.clear();
			data.addAll(_list);
		}
		if (null != observer) {
			observer.onChanged();
		}
	}

	public void collapseExpand(int position, boolean notify) {
		NoteNG note = getItem(position);
		clicked = note.id;
		if (!note.isExpandable()) {
			return;
		}
		selected = note.id;
		if (note.expanded == NoteNG.EXPAND_COLLAPSED) {
			expandNote(note, position, false);
		} else if (note.expanded == NoteNG.EXPAND_ONE){
			collapseNote(note, position);
			expandNote(note, position, true);
		} else {
			collapseNote(note, position);
		}
		if (notify && null != observer) {
			observer.onChanged();
		}
	}

	private void collapseNote(NoteNG note, int position) {
		note.expanded = NoteNG.EXPAND_COLLAPSED;
		int i = position + 1;
		while (i < data.size()) {
			if (data.get(i).indent <= note.indent) {
				break;
			}
			collapseNote(data.get(i), i);
			data.remove(i);
		}
	}

	private int expandNote(NoteNG note, int position, boolean expandAll) {
		note.expanded = expandAll? NoteNG.EXPAND_MANY: NoteNG.EXPAND_ONE;
		List<NoteNG> list = controller.getData(note.id);
		if (null == list) {
			return 0;
		}
		int pos = 0;
		for (int i = 0; i < list.size(); i++) {
			NoteNG n = list.get(i);
			n.indent = note.indent + 1;
			pos++;
			data.add(position + pos, n);
			if (expandAll) {
				pos += expandNote(n, position + pos, expandAll);
			}
		}
		return pos;
	}
	
	public String getIntent(int position) {
		NoteNG note = data.get(position);
		String noteID = note.originalID;
		if (null == noteID) {
			noteID = note.noteID;
		}
		if (null != noteID) {
			return "id:"+noteID;
		}
		return noteID;
	}
	
	public void setShowWide(boolean wide) {
		this.wide  = wide;
		if (null != observer) {
			observer.onChanged();
		}
	}

}
