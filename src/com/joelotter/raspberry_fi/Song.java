package com.joelotter.raspberry_fi;

public class Song {
	private String name;
	private String artist;
	private String id;
	private int up;
	private int down;
	private String song_id;
	
	public Song(String name, String artist, String id, int up, int down, String song_id){
		this.name = name;
		this.artist = artist;
		this.id = id;
		this.up = up;
		this.down = down;
		this.song_id = song_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUp() {
		return Integer.toString(up);
	}

	public void setUp(int up) {
		this.up = up;
	}

	public String getDown() {
		return Integer.toString(down);
	}

	public void setDown(int down) {
		this.down = down;
	}
	
	public String getSongId(){
		return song_id;
	}
	
}
