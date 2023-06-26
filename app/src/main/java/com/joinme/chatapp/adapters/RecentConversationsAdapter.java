package com.joinme.chatapp.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.renderscript.Sampler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joinme.chatapp.R;
import com.joinme.chatapp.databinding.ItemContainerRecentConversationBinding;
import com.joinme.chatapp.listeners.ConversionListener;
import com.joinme.chatapp.models.ChatMessage;
import com.joinme.chatapp.models.User;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private PreferenceManager preferenceManager;
    private Context mContext;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener, Context context) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.mContext = context;
        this.preferenceManager = new PreferenceManager(mContext);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        ItemContainerRecentConversationBinding binding;

        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding){
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatMessage chatMessage){
            Objects.requireNonNull(binding.imageProfile).setImageBitmap(getConversationImage(chatMessage.conversationImage));
            Objects.requireNonNull(binding.textName).setText(chatMessage.conversationName);
            Objects.requireNonNull(binding.textRecentMessage).setText(chatMessage.message);
            Objects.requireNonNull(binding.textDate).setText(getReadableDateTime(chatMessage.dateObject));
            if(!chatMessage.lastSenderId.equals(preferenceManager.getString(Constants.KEY_USER_ID)) && !chatMessage.seen){
                binding.textRecentMessage.setTypeface(null, Typeface.BOLD);
                Resources res = mContext.getResources();
                int color = res.getColor(R.color.unread);
                binding.textRecentMessage.setTextColor(color);
                binding.textName.setTypeface(null, Typeface.BOLD);
                binding.textName.setTextColor(color);
            }
            binding.getRoot().setOnClickListener( v -> {
                User user = new User();
                user.id = chatMessage.conversationId;
                user.name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                user.gender = chatMessage.conversationGender;
                user.age = chatMessage.conversationAge;
                user.shortBio = chatMessage.conversationBio;
                conversionListener.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
