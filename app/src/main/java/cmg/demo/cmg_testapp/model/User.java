package cmg.demo.cmg_testapp.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by alprokof on 9/30/2016.
 * This class is required to parse single user details received in JSON response.
 */

@Parcel
public class User {

    @SerializedName("login")
    String login;

    @SerializedName("id")
    String id;

    @SerializedName("avatar_url")
    String avatarUrl;

    @SerializedName("gravatar_id")
    String gravatarId;

    @SerializedName("url")
    String profileUrl;

    @SerializedName("html_url")
    String profileHtmlUrl;

    @SerializedName("followers_url")
    String followersUrl;

    @SerializedName("gists_url")
    String gistsUrl;

    @SerializedName("starred_url")
    String starredUrl;

    @SerializedName("subscriptions_url")
    String subscriptionsUrl;

    @SerializedName("organizations_url")
    String organizationsUrl;

    @SerializedName("repos_url")
    String reposUrl;

    @SerializedName("events_url")
    String eventsUrl;

    @SerializedName("received_events_url")
    String receivedEventsUrl;

    @SerializedName("type")
    String type;

    @SerializedName("site_admin")
    boolean siteAdmin;

    // Optional data for further app improvement
    @SerializedName("name")
    String name;

    @SerializedName("company")
    String company;

    @SerializedName("blog")
    String blog;

    @SerializedName("location")
    String location;

    @SerializedName("email")
    String email;

    @SerializedName("hireable")
    String hireable;

    @SerializedName("bio")
    String bio;

    @SerializedName("public_repos")
    int publicRepos;

    @SerializedName("public_gists")
    int publicGists;

    @SerializedName("followers")
    int followers;

    @SerializedName("following")
    int following;

    @SerializedName("created_at")
    String createdAt;

    @SerializedName("updated_at")
    String updatedAt;

    public String getLogin() {
        return login;
    }

    public String getId() {
        return id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
