package developer.shivam.joyplayer.Model;

public class Song {

    int id;
    int length;
    String name;
    String singer;
    String album;
    long albumId;

    public void setDetails(int id, String name, long albumId, String singer, int length) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.length = length;
        this.albumId = albumId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getSinger() {
        return singer;
    }

    public String getAlbum() {
        return album;
    }
}
