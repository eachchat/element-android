package im.vector.app.eachchat.department;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import im.vector.app.eachchat.contact.ContactSyncUtils;
import im.vector.app.eachchat.contact.api.bean.Department;
import im.vector.app.eachchat.contact.data.ContactsDisplayBean;
import im.vector.app.eachchat.contact.data.User;
import im.vector.app.eachchat.contact.database.ContactDaoHelper;
import im.vector.app.eachchat.department.data.DepartmentBean;
import im.vector.app.eachchat.department.data.DepartmentUserBean;
import im.vector.app.eachchat.department.data.IDisplayBean;
import im.vector.app.eachchat.rx.SimpleObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentModel {

    private DepartmentContract.Presenter mPresenter;

    public DepartmentModel(DepartmentContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    public void initData(String departmentId) {
        final long time = System.currentTimeMillis();
        AtomicBoolean isAllUser = new AtomicBoolean(true);
        AtomicInteger showMembersTagPos = new AtomicInteger(-1);
        Observable.create((ObservableOnSubscribe<List<IDisplayBean>>)
                emitter -> {
                    List<IDisplayBean> results = new ArrayList<>();
                    // Update: First load the department children, then users
                    //根据departmentId 查找子部门
                    List<IDisplayBean> departments;
                    if (TextUtils.isEmpty(departmentId)) {
                        //根据需求 不显示根部门 直接显示根部门下面的一级部门
                        List<IDisplayBean> roots = DepartmentStoreHelper.getRootDepartments();
                        departments = new ArrayList<>();
                        for (IDisplayBean root : roots) {
                            List<IDisplayBean> temps = DepartmentStoreHelper.getDepartmentsByParentId(root.getId());
                            departments.addAll(temps);
                        }
                    } else if (DepartmentFragment.ROOT_ID.equals(departmentId)) {
                        departments = new ArrayList<>();
                        departments.add(ContactUtils.generateGroupChat());
                        if (ContactSyncUtils.getInstance().isOpenOrg()) {
                            Department department = new Department();
                            department.setId(null);
                            department.setDisplayName(DepartmentFragment.ROOT_ID);
                            DepartmentBean departmentBean = new DepartmentBean(department);
                            departments.add(departmentBean);
                        }
                    } else {
                        departments = DepartmentStoreHelper.getDepartmentsByParentId(departmentId);
                    }
                    if (departments != null && departments.size() > 0) {
                        isAllUser.set(false);
                        results.addAll(departments);
                    }
                    //根据departmentId 查找部门成员
                    List<IDisplayBean> departmentUsers;
                    if (DepartmentFragment.ROOT_ID.equals(departmentId)) {
                        List<ContactsDisplayBean> contacts = ContactDaoHelper.Companion.getInstance().getContacts();
                        departmentUsers = new ArrayList<>();
                        for (ContactsDisplayBean contact : contacts) {
                            User user = contact.toUser();
                            DepartmentUserBean departmentUserBean = new DepartmentUserBean(user);
                            departmentUsers.add(departmentUserBean);
                        }
                    } else {
                        departmentUsers = UserStoreHelper.getSelectUsersByDepartmentId(departmentId);
                    }
                    results.addAll(departmentUsers);
                    if (departmentUsers.size() > 0) {
                        if (departments.size() > 0) {
                            showMembersTagPos.set(departments.size() - 1);
                        } else if (DepartmentFragment.ROOT_ID.equals(departmentId)) {
                            showMembersTagPos.set(departmentUsers.size() - 1);
                        }
                    }
                    long t = System.currentTimeMillis() - time;
                    Timber.e("时长" + t + "");
                    emitter.onNext(results);
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<IDisplayBean>>() {
                    @Override
                    public void onNext(List<IDisplayBean> departments) {
                        mPresenter.loadData(departments, isAllUser.get(), showMembersTagPos.get());
                    }
                });
    }

}
