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
import com.example.chat.models.Group;
import com.example.chat.utils.TimeUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groupsList;
    private OnGroupActionListener listener;
    private Context context;

    public interface OnGroupActionListener {
        void onGroupClick(Group group);
        void onGroupLongClick(Group group);
        void onGroupInfo(Group group);
        void onLeaveGroup(Group group);
    }

    public GroupsAdapter(List<Group> groupsList, OnGroupActionListener listener) {
        this.groupsList = groupsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageViewAvatar;
        private TextView textViewGroupName;
        private TextView textViewDescription;
        private TextView textViewMemberCount;
        private TextView textViewRole;
        private TextView textViewCreatedAt;
        private MaterialButton buttonMore;
        private View groupClickArea;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewMemberCount = itemView.findViewById(R.id.textViewMemberCount);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewCreatedAt = itemView.findViewById(R.id.textViewCreatedAt);
            buttonMore = itemView.findViewById(R.id.buttonMore);
            groupClickArea = itemView.findViewById(R.id.groupClickArea);
        }

        public void bind(Group group) {
            // Set group name
            textViewGroupName.setText(group.getName());

            // Set description
            if (group.getDescription() != null && !group.getDescription().isEmpty()) {
                textViewDescription.setText(group.getDescription());
                textViewDescription.setVisibility(View.VISIBLE);
            } else {
                textViewDescription.setVisibility(View.GONE);
            }

            // Set member count
            String memberText = group.getMemberCount() + " member" + (group.getMemberCount() > 1 ? "s" : "");
            textViewMemberCount.setText(memberText);

            // Set user role
            if (group.getUserRole() != null) {
                textViewRole.setText(group.getUserRole());
                textViewRole.setVisibility(View.VISIBLE);

                // Set role color
                if (group.isOwner()) {
                    textViewRole.setTextColor(context.getColor(R.color.colorPrimary));
                } else if (group.isAdmin()) {
                    textViewRole.setTextColor(context.getColor(R.color.success_color));
                } else {
                    textViewRole.setTextColor(context.getColor(R.color.text_color_secondary));
                }
            } else {
                textViewRole.setVisibility(View.GONE);
            }

            // Set created date
            if (group.getCreatedAt() != null) {
                String createdText = "Created " + TimeUtils.getRelativeTime(group.getCreatedAt());
                textViewCreatedAt.setText(createdText);
                textViewCreatedAt.setVisibility(View.VISIBLE);
            } else {
                textViewCreatedAt.setVisibility(View.GONE);
            }

            // Set group avatar
            loadGroupAvatar(group);

            // Set click listeners
            groupClickArea.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupClick(group);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onGroupLongClick(group);
                    return true;
                }
                return false;
            });

            buttonMore.setOnClickListener(v -> {
                showMoreOptions(group);
            });
        }

        private void loadGroupAvatar(Group group) {
            String avatarUrl = group.getDisplayAvatar();

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .circleCrop();

            Glide.with(context)
                    .load(avatarUrl)
                    .apply(requestOptions)
                    .into(imageViewAvatar);
        }

        private void showMoreOptions(Group group) {
            try {
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(context, buttonMore);
                popup.getMenuInflater().inflate(R.menu.group_options_menu, popup.getMenu());

                // Hide leave option for owners
                if (group.isOwner()) {
                    popup.getMenu().findItem(R.id.action_leave_group).setVisible(false);
                }

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_group_info) {
                        if (listener != null) {
                            listener.onGroupInfo(group);
                        }
                        return true;
                    } else if (itemId == R.id.action_leave_group) {
                        if (listener != null) {
                            listener.onLeaveGroup(group);
                        }
                        return true;
                    }
                    return false;
                });

                popup.show();
            } catch (Exception e) {
                // If popup menu fails, just call group info
                if (listener != null) {
                    listener.onGroupInfo(group);
                }
            }
        }
    }

    /**
     * Update groups list
     */
    public void updateGroups(List<Group> newGroups) {
        this.groupsList.clear();
        this.groupsList.addAll(newGroups);
        notifyDataSetChanged();
    }

    /**
     * Add more groups (for pagination)
     */
    public void addGroups(List<Group> newGroups) {
        int startPosition = this.groupsList.size();
        this.groupsList.addAll(newGroups);
        notifyItemRangeInserted(startPosition, newGroups.size());
    }

    /**
     * Remove group from list
     */
    public void removeGroup(Group group) {
        int position = groupsList.indexOf(group);
        if (position != -1) {
            groupsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clear all groups
     */
    public void clearGroups() {
        groupsList.clear();
        notifyDataSetChanged();
    }
}
