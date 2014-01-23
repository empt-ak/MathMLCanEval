/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.forms;

import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Empt
 */
public class UserForm
{
    private Long id;    
    @Size(min = 3, max = 255)
    private String username;
    @Size(min = 3, max = 255)
    private String realName; 
    @NotNull
    private String password;
    @NotNull
    private String passwordVerify;
    private List<UserRoleForm> userRoleForms;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPasswordVerify()
    {
        return passwordVerify;
    }

    public void setPasswordVerify(String passwordVerify)
    {
        this.passwordVerify = passwordVerify;
    }

    public List<UserRoleForm> getUserRoleForms()
    {
        return userRoleForms;
    }

    public void setUserRoleForms(List<UserRoleForm> userRoleForms)
    {
        this.userRoleForms = userRoleForms;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final UserForm other = (UserForm) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString()
    {
        return "User{" + "id=" + id + ", username=" + username + ", password=" + password + ", userRoles=" + userRoleForms + '}';
    }
}
