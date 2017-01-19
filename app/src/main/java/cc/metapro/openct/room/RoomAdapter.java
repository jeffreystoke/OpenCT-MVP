package cc.metapro.openct.room;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.RoomInfo;

class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RoomInfo> mRoomInfos;

    private Context mContext;

    RoomAdapter(Context context) {
        mContext = context;
        mRoomInfos = new ArrayList<>(0);
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        RoomInfo info = mRoomInfos.get(position);
        holder.setRoomPlace(info.getPlace());
        holder.setAllAvailableTime(mContext, info.getAllAvailTime());
    }

    @Override
    public int getItemCount() {
        return mRoomInfos.size();
    }

    public void setRoomInfos(List<RoomInfo> roomInfos) {
        if (roomInfos != null) {
            mRoomInfos = roomInfos;
        } else {
            mRoomInfos = new ArrayList<>(0);
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_room_place)
        TextView mRoomPlace;

        public RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setRoomPlace(String place) {
            mRoomPlace.setText(place);
        }

        public void setAllAvailableTime(final Context context, final String availTime) {
            mRoomPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle("教室空闲时间");
                    ab.setMessage(availTime);
                    ab.show();
                    ab.setPositiveButton("返回", null);
                }
            });
        }
    }
}
