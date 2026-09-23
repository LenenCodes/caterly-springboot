package com.caters.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "caterers")
public class Caterers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@Column(name = "business_name")
	private String businessName;

	private String phone;

	private String address;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "video_url")
	private String videoUrl;

	@Column(name = "video_file_path")
	private String videoFilePath;

	@Column(name = "banner_image_url")
	private String bannerImageUrl;

	@Column(name = "is_verified")
	private boolean isVerified = false;
	@Column(name = "profile_image_url")
	private String profileImageUrl;

	public Caterers() {
	}

	public Caterers(Users user, String businessName, String phone, String address) {
		this.user = user;
		this.businessName = businessName;
		this.phone = phone;
		this.address = address;
		this.isVerified = false;
	}

	public Caterers(Users user, String businessName, String phone, String address, String description, String videoUrl,
			String videoFilePath, boolean isVerified) {
		this.user = user;
		this.businessName = businessName;
		this.phone = phone;
		this.address = address;
		this.description = description;
		this.videoUrl = videoUrl;
		this.videoFilePath = videoFilePath;
		this.isVerified = isVerified;
	}

	// Helper to produce a safe embed URL for YouTube / Vimeo / direct video
	public String getEmbedVideoUrl() {
		if (this.videoUrl == null || this.videoUrl.isBlank()) {
			return null;
		}

		String url = this.videoUrl.trim();

		// Standard YouTube URL: https://www.youtube.com/watch?v=VIDEO_ID
		if (url.contains("youtube.com/watch?v=")) {
			String videoId = url.substring(url.indexOf("watch?v=") + 8);
			int ampIndex = videoId.indexOf('&');
			if (ampIndex != -1) {
				videoId = videoId.substring(0, ampIndex);
			}
			return "https://www.youtube.com/embed/" + videoId;
		}

		// Shortened YouTube URL: https://youtu.be/VIDEO_ID
		if (url.contains("youtu.be/")) {
			String videoId = url.substring(url.indexOf("youtu.be/") + 9);
			int queryIndex = videoId.indexOf('?');
			if (queryIndex != -1) {
				videoId = videoId.substring(0, queryIndex);
			}
			return "https://www.youtube.com/embed/" + videoId;
		}

		// Vimeo: https://vimeo.com/VIDEO_ID
		if (url.contains("vimeo.com/") && !url.contains("player.vimeo.com")) {
			String videoId = url.substring(url.lastIndexOf('/') + 1);
			return "https://player.vimeo.com/video/" + videoId;
		}

		// Already an embed URL or direct hosted link
		return url;
	}

	// ==========================================
	// GETTERS & SETTERS
	// ==========================================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getVideoFilePath() {
		return videoFilePath;
	}

	public void setVideoFilePath(String videoFilePath) {
		this.videoFilePath = videoFilePath;
	}

	public String getBannerImageUrl() {
		return bannerImageUrl;
	}

	public void setBannerImageUrl(String bannerImageUrl) {
		this.bannerImageUrl = bannerImageUrl;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean verified) {
		isVerified = verified;
	}
	public String getProfileImageUrl() {
	    return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
	    this.profileImageUrl = profileImageUrl;
	}
}