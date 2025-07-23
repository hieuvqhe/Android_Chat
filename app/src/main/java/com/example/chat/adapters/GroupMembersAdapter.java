package com.example.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.R;
import com.example.chat.models.GroupMember;
import com.example.chat.models.UserInfo;
import com.example.chat.utils.TimeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder> {

    private List<GroupMember> membersList;
    private OnMemberActionListener listener;
    private Context context;

    public interface OnMemberActionListener {
        void onMemberClick(GroupMember member);
        void onMemberLongClick(GroupMember member);
        void onRemoveMember(GroupMember member);
        void onMakeAdmin(GroupMember member);
        void onRemoveAdmin(GroupMember member);
    }

    public GroupMembersAdapter(List<GroupMember> membersList, OnMemberActionListener listener) {
        this.membersList = membersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        GroupMember member = membersList.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private ImageView imageViewVerified;
        private ImageView imageViewOnlineStatus;
        private TextView textViewUsername;
        private TextView textViewEmail;
        private TextView textViewRole;
        private TextView textViewJoinedDate;
        private MaterialButton buttonMore;
        private View clickArea;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            imageViewVerified = itemView.findViewById(R.id.imageViewVerified);
            imageViewOnlineStatus = itemView.findViewById(R.id.imageViewOnlineStatus);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewJoinedDate = itemView.findViewById(R.id.textViewJoinedDate);
            buttonMore = itemView.findViewById(R.id.buttonMore);
            clickArea = itemView.findViewById(R.id.clickArea);
        }

        public void bind(GroupMember member) {
            UserInfo user = member.getUser();
            if (user == null) return;

            // Set username
            String username = user.getUsername() != null ? user.getUsername() : "Unknown User";
            textViewUsername.setText(username);

            // Set email
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                textViewEmail.setText(user.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }

            // Set role
            String role = member.getRole();
            if (role != null) {
                textViewRole.setText(role);
                textViewRole.setVisibility(View.VISIBLE);

                // Set role styling
                if (member.isOwner()) {
                    textViewRole.setTextColor(context.getColor(R.color.colorPrimary));
                    textViewRole.setBackground(context.getDrawable(R.drawable.status_accepted_background));
                } else if (member.isAdmin()) {
                    textViewRole.setTextColor(context.getColor(R.color.success_color));
                    textViewRole.setBackground(context.getDrawable(R.drawable.status_pending_background));
                } else {
                    textViewRole.setTextColor(context.getColor(R.color.text_color_secondary));
                    textViewRole.setBackground(null);
                }
            } else {
                textViewRole.setVisibility(View.GONE);
            }

            // Set joined date
            if (member.getJoinedAt() != null) {
                String joinedText = "Joined " + TimeUtils.getRelativeTime(member.getJoinedAt());
                textViewJoinedDate.setText(joinedText);
                textViewJoinedDate.setVisibility(View.VISIBLE);
            } else {
                textViewJoinedDate.setVisibility(View.GONE);
            }

            // Set avatar
            loadUserAvatar(user);

            // Set verification badge
            boolean isVerified = user.getVerify() == 1;
            imageViewVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

            // Set online status
            boolean isOnline = user.getActiveStatus() == 1;
            imageViewOnlineStatus.setVisibility(isOnline ? View.VISIBLE : View.GONE);

            // Set click listeners
            clickArea.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberClick(member);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMemberLongClick(member);
                    return true;
                }
                return false;
            });

            buttonMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberLongClick(member);
                }
            });
        }

        private void loadUserAvatar(UserInfo user) {
            String avatarUrl = user.getAvatar();

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop();

            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                Glide.with(context)
                        .load(avatarUrl)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.default_avatar)
                        .apply(requestOptions)
                        .into(imageViewAvatar);
            }
        }
    }

    /**
     * Update members list
     */
    public void updateMembers(List<GroupMember> newMembers) {
        this.membersList.clear();
        this.membersList.addAll(newMembers);
        notifyDataSetChanged();
    }

    /**
     * Add more members (for pagination)
     */
    public void addMembers(List<GroupMember> newMembers) {
        int startPosition = this.membersList.size();
        this.membersList.addAll(newMembers);
        notifyItemRangeInserted(startPosition, newMembers.size());
    }

    /**
     * Remove member from list
     */
    public void removeMember(GroupMember member) {
        int position = membersList.indexOf(member);
        if (position != -1) {
            membersList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update specific member
     */
    public void updateMember(GroupMember member) {
        int position = findMemberPosition(member);
        if (position != -1) {
            membersList.set(position, member);
            notifyItemChanged(position);
        }
    }

    /**
     * Clear all members
     */
    public void clearMembers() {
        membersList.clear();
        notifyDataSetChanged();
    }

    /**
     * Find member position by ID
     */
    private int findMemberPosition(GroupMember member) {
        for (int i = 0; i < membersList.size(); i++) {
            if (membersList.get(i).getId().equals(member.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get member at position
     */
    public GroupMember getMemberAt(int position) {
        if (position >= 0 && position < membersList.size()) {
            return membersList.get(position);
        }
        return null;
    }
}