package com.onetap.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.onetap.app.R;
import com.onetap.app.models.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<Session> sessions;
    private OnSessionActionListener listener;
    private boolean showEndButton;

    public interface OnSessionActionListener {
        void onEndSession(Session session);
        void onSessionClick(Session session);
        void onDownloadAttendance(Session session); // ✅ NEW
    }

    public SessionAdapter(List<Session> sessions, OnSessionActionListener listener,
                          boolean showEndButton) {
        this.sessions = sessions;
        this.listener = listener;
        this.showEndButton = showEndButton;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateData(List<Session> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvSubjectName, tvSessionCode,
                tvTotalStudents, tvDuration, tvStatus;
        MaterialButton btnEndSession, btnDownload; // ✅ NEW

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSessionCode = itemView.findViewById(R.id.tvSessionCode);
            tvTotalStudents = itemView.findViewById(R.id.tvTotalStudents);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEndSession = itemView.findViewById(R.id.btnEndSession);
            btnDownload = itemView.findViewById(R.id.btnDownload); // ✅ NEW
        }

        void bind(Session session) {
            tvSessionTitle.setText(session.getSessionTitle());
            tvSubjectName.setText(session.getSubjectName());
            tvSessionCode.setText(session.getSessionCode());
            tvTotalStudents.setText(String.valueOf(session.getTotalStudents()));
            tvDuration.setText(session.getDuration() + " min");

            if (session.isActive()) {
                tvStatus.setText("Active");
                tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.accent_green));
            } else {
                tvStatus.setText("Ended");
                tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.accent_red));
            }

            // End session button
            btnEndSession.setVisibility(showEndButton && session.isActive() ?
                    View.VISIBLE : View.GONE);
            btnEndSession.setOnClickListener(v -> {
                if (listener != null) listener.onEndSession(session);
            });

            // ✅ Download button - always visible
            if (btnDownload != null) {
                btnDownload.setVisibility(View.VISIBLE);
                btnDownload.setOnClickListener(v -> {
                    if (listener != null) listener.onDownloadAttendance(session);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSessionClick(session);
            });
        }
    }
}