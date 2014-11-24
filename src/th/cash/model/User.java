package th.cash.model;

/**
 * Created by IntelliJ IDEA.
 * User: lazarev
 * Date: 15.11.2007
 * Time: 17:11:03
 * To change this template use File | Settings | File Templates.
 */
public class User implements Comparable{

    private Integer code;
    private String name;
    private Integer np;
    private String password;
    private String cr_pwd;
//    private Integer frcode;     // ����� ������������ � ���������� ����������
                              // ���������������� ��� ������ � ������� ��
    private Role role;                           

    public User(Integer code, String name, Integer np, String password) {
        this.code = code;
        this.name = name;
        this.np = np;
        try { // decode � ������������ �������
        this.password = new PwdUserDecode().decode(cr_pwd = password);
        } catch (Exception ex)
        {
          throw new RuntimeException(ex.getMessage());
        }
        // TODO - ��� ������������ ������ ������������ ?
        // ��� ������ �� ������� ��� ��� ������ �������� � ������� �������� ������?
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getNp() {
        return np;
    }

    public String getPassword() {
        return password;
    }

    public String getHexPassword() {
      return cr_pwd;
    }


    public Role getRole()
    {
      return role;
    }

    public void setRole(Role newRole)
    {
      role = newRole;
    }

    public String toString()
    {
      return getName();
    }

    

    public int compareTo(Object o) {
        return getCode().compareTo(((User)o).getCode());
    }


}
