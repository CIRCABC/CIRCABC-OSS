package eu.cec.digit.circabc.repo.app.model;

public class ProfileWithUsersCount extends Profile {

    private int numberOfUsers;

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }
}
