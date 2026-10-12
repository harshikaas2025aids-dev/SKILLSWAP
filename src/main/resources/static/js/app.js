/**
 * SkillSwap - Frontend Application Controller
 * Handles REST API communication, active student state, UI rendering, and reactive modals/toasts.
 */

const SkillSwap = (() => {
  // Configurable API endpoints
  const API = {
    students: '/api/students',
    skills: '/api/skills',
    search: '/api/skills/search',
    matches: '/api/matches',
    studentMatches: (id) => `/api/students/${id}/matches`,
    studentSkills: (id) => `/api/students/${id}/skills`,
    requests: '/api/requests',
    sentRequests: (id) => `/api/requests/sent/${id}`,
    receivedRequests: (id) => `/api/requests/received/${id}`,
    acceptRequest: (id) => `/api/requests/${id}/accept`,
    rejectRequest: (id) => `/api/requests/${id}/reject`,
    cancelRequest: (id) => `/api/requests/${id}/cancel`,
    completeRequest: (id) => `/api/requests/${id}/complete`,
    dashboardStats: (id) => `/api/dashboard/stats?studentId=${id}`
  };

  // Determine active student ID from query param or localStorage (default: 1 - Arun)
  function getActiveStudentId() {
    const urlParams = new URLSearchParams(window.location.search);
    const paramId = urlParams.get('studentId');
    if (paramId && !isNaN(parseInt(paramId))) {
      localStorage.setItem('skillswap_student_id', paramId);
      return parseInt(paramId);
    }
    const stored = localStorage.getItem('skillswap_student_id');
    return stored ? parseInt(stored) : 1;
  }

  let activeStudentId = getActiveStudentId();
  let currentStudentData = null;

  // Utility: HTTP Fetch with JSON handling
  async function request(url, options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
    try {
      const response = await fetch(url, {
        ...options,
        headers: { ...defaultHeaders, ...(options.headers || {}) }
      });
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || `HTTP error ${response.status}`);
      }
      return data;
    } catch (err) {
      console.error(`[API Error] ${url}:`, err);
      throw err;
    }
  }

  // Toast Notification System
  function showToast(message, type = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = `
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
        <polyline points="22 4 12 14.01 9 11.01"></polyline>
      </svg>`;
    if (type === 'error') {
      icon = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#D32F2F" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>`;
    } else if (type === 'warning') {
      icon = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FFA000" stroke-width="2">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>`;
    }

    toast.innerHTML = `
      <div>${icon}</div>
      <div style="flex:1; font-weight:600; font-size:0.9rem; color:var(--text-dark);">${message}</div>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }

  // Confirmation / Dynamic Modal
  function showModal({ title, content, confirmText = 'Confirm', onConfirm }) {
    let overlay = document.getElementById('globalModalOverlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'globalModalOverlay';
      overlay.className = 'modal-overlay';
      overlay.innerHTML = `
        <div class="modal-container">
          <div class="modal-header">
            <h3 id="modalTitle">${title}</h3>
            <button class="modal-close-btn" onclick="SkillSwap.closeModal()">&times;</button>
          </div>
          <div id="modalBody" style="margin-bottom:24px;"></div>
          <div style="display:flex; justify-content:flex-end; gap:12px;">
            <button class="btn btn-secondary" onclick="SkillSwap.closeModal()">Cancel</button>
            <button id="modalConfirmBtn" class="btn btn-forest">${confirmText}</button>
          </div>
        </div>
      `;
      document.body.appendChild(overlay);
    } else {
      document.getElementById('modalTitle').textContent = title;
      document.getElementById('modalConfirmBtn').textContent = confirmText;
    }

    const body = document.getElementById('modalBody');
    if (typeof content === 'string') {
      body.innerHTML = content;
    } else {
      body.innerHTML = '';
      body.appendChild(content);
    }

    const confirmBtn = document.getElementById('modalConfirmBtn');
    confirmBtn.onclick = async () => {
      if (onConfirm) {
        confirmBtn.disabled = true;
        confirmBtn.textContent = 'Processing...';
        try {
          await onConfirm();
          closeModal();
        } catch (e) {
          showToast(e.message, 'error');
        } finally {
          confirmBtn.disabled = false;
          confirmBtn.textContent = confirmText;
        }
      } else {
        closeModal();
      }
    };

    setTimeout(() => overlay.classList.add('active'), 10);
  }

  function closeModal() {
    const overlay = document.getElementById('globalModalOverlay');
    if (overlay) {
      overlay.classList.remove('active');
    }
  }

  // Sync active student profile in UI (header + sidebar)
  async function syncActiveStudent() {
    try {
      const res = await request(`${API.students}/${activeStudentId}`);
      if (res.success && res.data) {
        currentStudentData = res.data;
        updateUIWithStudent(res.data);
      }
    } catch (err) {
      console.warn('Could not load active student details. Using fallback ID:', activeStudentId);
      updateUIWithStudent({ studentId: activeStudentId, studentName: 'Active Student #' + activeStudentId });
    }
  }

  function updateUIWithStudent(student) {
    const nameEls = document.querySelectorAll('.active-student-name');
    nameEls.forEach(el => el.textContent = student.studentName);

    const idEls = document.querySelectorAll('.active-student-id');
    idEls.forEach(el => el.textContent = `#${student.studentId}`);

    const avatarEls = document.querySelectorAll('.active-student-avatar');
    const initials = student.studentName ? student.studentName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'S';
    avatarEls.forEach(el => el.textContent = initials);

    // Update query params in navigation links to keep session consistent across pages
    document.querySelectorAll('.nav-item').forEach(link => {
      const href = link.getAttribute('href');
      if (href && !href.startsWith('#') && href !== '/register') {
        const url = new URL(href, window.location.origin);
        url.searchParams.set('studentId', student.studentId);
        link.setAttribute('href', url.pathname + url.search);
      }
    });
  }

  // Switch Student Modal
  async function promptSwitchStudent() {
    try {
      const res = await request(API.students);
      if (!res.success || !res.data) return;

      const students = res.data;
      let html = `<p style="font-size:0.92rem; color:var(--text-light); margin-bottom:16px;">
        Select an active student profile to experience SkillSwap from their perspective:
      </p>
      <div style="display:flex; flex-direction:column; gap:8px; max-height:280px; overflow-y:auto;">`;

      students.forEach(s => {
        const isCurrent = s.studentId === activeStudentId;
        html += `
          <div onclick="SkillSwap.switchStudent(${s.studentId})" 
               style="padding:12px 14px; border:1px solid ${isCurrent ? 'var(--deep-forest)' : 'var(--border-color)'}; 
                      background:${isCurrent ? 'var(--soft-mint)' : 'var(--card-white)'}; border-radius:var(--radius-md); 
                      cursor:pointer; display:flex; align-items:center; justify-content:space-between; transition:var(--transition);">
            <div>
              <div style="font-weight:700; color:var(--text-dark);">${s.studentName}</div>
              <div style="font-size:0.8rem; color:var(--text-muted);">${s.email} | ID #${s.studentId}</div>
            </div>
            ${isCurrent ? '<span class="badge badge-teach">ACTIVE</span>' : '<span style="font-size:0.85rem; color:var(--deep-forest); font-weight:600;">Select &rarr;</span>'}
          </div>`;
      });
      html += `</div>`;

      showModal({
        title: 'Switch Active Student',
        content: html,
        confirmText: 'Done',
        onConfirm: () => closeModal()
      });
    } catch (e) {
      showToast('Failed to load students: ' + e.message, 'error');
    }
  }

  function switchStudent(id) {
    activeStudentId = id;
    localStorage.setItem('skillswap_student_id', id);
    closeModal();
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('studentId', id);
    window.location.href = currentUrl.toString();
  }

  // Mobile drawer toggle
  function toggleSidebar() {
    const sidebar = document.querySelector('.app-sidebar');
    if (sidebar) sidebar.classList.toggle('open');
  }

  // Initialize Common UI (Navbar, Sidebar, Global Search)
  function initCommon() {
    syncActiveStudent();

    const mobileBtn = document.querySelector('.mobile-menu-btn');
    if (mobileBtn) {
      mobileBtn.addEventListener('click', toggleSidebar);
    }

    const searchInput = document.getElementById('globalSearchInput');
    if (searchInput) {
      searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && searchInput.value.trim()) {
          window.location.href = `/search?keyword=${encodeURIComponent(searchInput.value.trim())}&studentId=${activeStudentId}`;
        }
      });
    }
  }

  // ---------------------------------------------------------------------------
  // Page Controllers
  // ---------------------------------------------------------------------------

  // A. Dashboard Page Controller
  async function initDashboard() {
    initCommon();
    const statsContainer = document.getElementById('dashboardStatsArea');
    const partnersContainer = document.getElementById('dashboardRecommendedPartners');
    const activityContainer = document.getElementById('dashboardRecentActivity');

    try {
      const res = await request(API.dashboardStats(activeStudentId));
      if (!res.success || !res.data) return;

      const data = res.data;

      // Update counters
      document.getElementById('statTotalStudents').textContent = data.totalStudents;
      document.getElementById('statSkillsOffered').textContent = data.skillsOffered;
      document.getElementById('statSkillsWanted').textContent = data.skillsWanted;
      document.getElementById('statPendingRequests').textContent = data.pendingRequests;
      document.getElementById('statAcceptedExchanges').textContent = data.acceptedExchanges;
      document.getElementById('statMutualMatches').textContent = data.mutualMatchesCount;

      // Render Recommended Partners (Mutual Matches)
      if (partnersContainer) {
        if (!data.recommendedPartners || data.recommendedPartners.length === 0) {
          partnersContainer.innerHTML = `
            <div class="empty-state-box">
              <div class="empty-state-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </div>
              <h4>No mutual matches found yet</h4>
              <p>Add more skills you teach and want to learn to discover reciprocal exchange partners!</p>
              <a href="/skills?studentId=${activeStudentId}" class="btn btn-primary btn-sm">Add New Skill</a>
            </div>`;
        } else {
          let html = `<div style="display:flex; flex-direction:column; gap:14px;">`;
          data.recommendedPartners.forEach(m => {
            html += `
              <div style="background:var(--card-white); border:1px solid var(--border-color); border-radius:var(--radius-md); 
                          padding:18px; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:16px; transition:var(--transition);"
                   onmouseover="this.style.boxShadow='var(--shadow-hover)'" onmouseout="this.style.boxShadow='none'">
                <div>
                  <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
                    <span style="font-weight:800; font-size:1.05rem; color:var(--deep-forest);">${m.partnerStudentName}</span>
                    <span class="badge badge-intermediate">100% Reciprocal Match</span>
                  </div>
                  <div style="font-size:0.86rem; color:var(--text-light); line-height:1.6;">
                    <span>&bull; You teach: <strong>${m.currentOfferedSkillName}</strong> [${m.currentOfferedProficiency}]</span><br>
                    <span>&bull; They teach: <strong>${m.partnerOfferedSkillName}</strong> [${m.partnerOfferedProficiency}]</span>
                  </div>
                </div>
                <button class="btn btn-forest btn-sm" 
                        onclick="SkillSwap.sendExchangePrompt(${m.currentStudentId}, ${m.partnerStudentId}, ${m.currentOfferedSkillId}, ${m.partnerOfferedSkillId}, '${m.partnerStudentName}')">
                  Request Swap &rarr;
                </button>
              </div>`;
          });
          html += `</div>`;
          partnersContainer.innerHTML = html;
        }
      }

      // Render Recent Activity
      if (activityContainer) {
        if (!data.recentActivity || data.recentActivity.length === 0) {
          activityContainer.innerHTML = `
            <div class="empty-state-box">
              <h4>No recent exchange activity</h4>
              <p>Sent or received requests will appear here in real time.</p>
              <a href="/matches?studentId=${activeStudentId}" class="btn btn-secondary btn-sm">Browse Matches</a>
            </div>`;
        } else {
          let html = `<div style="display:flex; flex-direction:column; gap:12px;">`;
          data.recentActivity.forEach(r => {
            const isSender = r.requesterId === activeStudentId;
            const otherName = isSender ? r.receiverName : r.requesterName;
            const statusClass = `status-${r.status.toLowerCase()}`;
            html += `
              <div style="background:#FAF8F1; border:1px solid var(--border-color); border-radius:var(--radius-md); padding:14px; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px;">
                <div>
                  <div style="font-size:0.92rem; font-weight:700; color:var(--text-dark);">
                    ${isSender ? `Sent to <strong>${otherName}</strong>` : `Received from <strong>${otherName}</strong>`}
                  </div>
                  <div style="font-size:0.82rem; color:var(--text-muted); margin-top:2px;">
                    Offering: <strong>${r.offeredSkillName}</strong> &bull; Wanting: <strong>${r.wantedSkillName}</strong>
                  </div>
                </div>
                <span class="status-badge ${statusClass}">${r.status}</span>
              </div>`;
          });
          html += `</div>`;
          activityContainer.innerHTML = html;
        }
      }

    } catch (e) {
      showToast('Error loading dashboard statistics: ' + e.message, 'error');
    }
  }

  // B. Registration Page Controller
  function initRegister() {
    initCommon();
    const form = document.getElementById('studentRegisterForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const name = document.getElementById('registerName').value.trim();
      const email = document.getElementById('registerEmail').value.trim();
      const contact = document.getElementById('registerContact').value.trim();

      if (!name || !email) {
        showToast('Please provide both name and a valid email.', 'warning');
        return;
      }

      const submitBtn = form.querySelector('button[type="submit"]');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Registering Profile...';

      try {
        const res = await request(API.students, {
          method: 'POST',
          body: JSON.stringify({ studentName: name, email, contactInfo: contact })
        });

        if (res.success && res.data) {
          showToast(`Welcome ${res.data.studentName}! Registered with ID #${res.data.studentId}`, 'success');
          // Switch to new student
          localStorage.setItem('skillswap_student_id', res.data.studentId);
          setTimeout(() => {
            window.location.href = `/skills?studentId=${res.data.studentId}`;
          }, 1200);
        }
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register Profile';
      }
    });
  }

  // C. Profile Page Controller
  async function initProfile() {
    initCommon();
    const container = document.getElementById('profileContentArea');
    if (!container) return;

    try {
      const [studentRes, skillsRes] = await Promise.all([
        request(`${API.students}/${activeStudentId}`),
        request(API.studentSkills(activeStudentId))
      ]);

      if (!studentRes.success || !studentRes.data) {
        container.innerHTML = `<div class="empty-state-box"><h4>Student not found</h4></div>`;
        return;
      }

      const student = studentRes.data;
      const skills = skillsRes.data || [];
      const teachSkills = skills.filter(s => s.skillType === 'TEACH');
      const learnSkills = skills.filter(s => s.skillType === 'LEARN');

      document.getElementById('profileCardName').textContent = student.studentName;
      document.getElementById('profileCardEmail').textContent = student.email;
      document.getElementById('profileCardContact').textContent = student.contactInfo || 'No contact details specified';
      document.getElementById('profileCardId').textContent = '#' + student.studentId;

      // Render Teach Skills
      const teachArea = document.getElementById('profileTeachSkillsArea');
      if (teachArea) {
        if (teachSkills.length === 0) {
          teachArea.innerHTML = `<p style="color:var(--text-muted); font-size:0.88rem;">No skills offered yet.</p>`;
        } else {
          teachArea.innerHTML = teachSkills.map(s => `
            <div style="background:var(--soft-mint); border:1px solid var(--primary-mint); border-radius:var(--radius-md); padding:12px 16px; margin-bottom:8px;">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <strong style="color:var(--deep-forest);">${s.skillName}</strong>
                <span class="badge badge-${s.proficiencyLevel.toLowerCase()}">${s.proficiencyLevel}</span>
              </div>
              ${s.description ? `<p style="font-size:0.82rem; color:var(--text-light); margin-top:4px;">${s.description}</p>` : ''}
            </div>
          `).join('');
        }
      }

      // Render Learn Skills
      const learnArea = document.getElementById('profileLearnSkillsArea');
      if (learnArea) {
        if (learnSkills.length === 0) {
          learnArea.innerHTML = `<p style="color:var(--text-muted); font-size:0.88rem;">No skills wanted yet.</p>`;
        } else {
          learnArea.innerHTML = learnSkills.map(s => `
            <div style="background:#FFF0E6; border:1px solid var(--soft-peach); border-radius:var(--radius-md); padding:12px 16px; margin-bottom:8px;">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <strong style="color:#8A4315;">${s.skillName}</strong>
                <span class="badge badge-${s.proficiencyLevel.toLowerCase()}">${s.proficiencyLevel}</span>
              </div>
              ${s.description ? `<p style="font-size:0.82rem; color:var(--text-light); margin-top:4px;">${s.description}</p>` : ''}
            </div>
          `).join('');
        }
      }

    } catch (e) {
      showToast('Error loading profile: ' + e.message, 'error');
    }
  }

  // D. Skills Management Page Controller
  async function initSkills() {
    initCommon();
    loadStudentSkillsList();

    // Hook skill creation modal/form
    const form = document.getElementById('addSkillForm');
    if (form) {
      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const skillName = document.getElementById('skillNameInput').value.trim();
        const skillType = document.getElementById('skillTypeSelect').value;
        const proficiencyLevel = document.getElementById('proficiencySelect').value;
        const description = document.getElementById('skillDescInput').value.trim();

        if (!skillName) {
          showToast('Please enter a skill name', 'warning');
          return;
        }

        try {
          const res = await request(API.skills, {
            method: 'POST',
            body: JSON.stringify({
              studentId: activeStudentId,
              skillName,
              skillType,
              proficiencyLevel,
              description
            })
          });

          if (res.success) {
            showToast(`Skill '${skillName}' registered successfully!`, 'success');
            form.reset();
            loadStudentSkillsList();
          }
        } catch (err) {
          showToast(err.message, 'error');
        }
      });
    }
  }

  async function loadStudentSkillsList() {
    const container = document.getElementById('skillsListContainer');
    if (!container) return;
    container.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Loading your skills...</p></div>`;

    try {
      const res = await request(API.studentSkills(activeStudentId));
      if (!res.success || !res.data || res.data.length === 0) {
        container.innerHTML = `
          <div class="empty-state-box">
            <div class="empty-state-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
            </div>
            <h4>No skills registered yet</h4>
            <p>Add at least one TEACH skill and one LEARN skill to start matching with other students!</p>
          </div>`;
        return;
      }

      const skills = res.data;
      let html = `<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:18px;">`;
      skills.forEach(s => {
        const isTeach = s.skillType === 'TEACH';
        html += `
          <div class="card" style="position:relative; border-top: 4px solid ${isTeach ? 'var(--deep-forest)' : 'var(--soft-peach-hover)'};">
            <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:12px;">
              <span class="badge ${isTeach ? 'badge-teach' : 'badge-learn'}">${s.skillType}</span>
              <span class="badge badge-${s.proficiencyLevel.toLowerCase()}">${s.proficiencyLevel}</span>
            </div>
            <h3 style="font-size:1.15rem; font-weight:800; color:var(--deep-forest); margin-bottom:6px;">${s.skillName}</h3>
            <p style="font-size:0.86rem; color:var(--text-light); min-height:42px; margin-bottom:16px;">
              ${s.description ? s.description : 'No additional description provided.'}
            </p>
            <div style="display:flex; justify-content:flex-end; border-top:1px solid var(--border-light); padding-top:12px;">
              <button class="btn btn-danger btn-sm" onclick="SkillSwap.confirmDeleteSkill(${s.skillId}, '${s.skillName}')">
                Delete Skill
              </button>
            </div>
          </div>`;
      });
      html += `</div>`;
      container.innerHTML = html;
    } catch (e) {
      container.innerHTML = `<div class="empty-state-box"><h4>Error loading skills</h4><p>${e.message}</p></div>`;
    }
  }

  function confirmDeleteSkill(skillId, skillName) {
    showModal({
      title: 'Delete Skill',
      content: `<p>Are you sure you want to delete <strong>${skillName}</strong>?</p>
                <p style="font-size:0.82rem; color:var(--text-muted); margin-top:8px;">Note: Skills referenced in active or historical exchange requests cannot be deleted.</p>`,
      confirmText: 'Delete',
      onConfirm: async () => {
        const res = await request(`${API.skills}/${skillId}?studentId=${activeStudentId}`, { method: 'DELETE' });
        if (res.success) {
          showToast(`Skill '${skillName}' deleted.`, 'success');
          loadStudentSkillsList();
        }
      }
    });
  }

  // E. Search Skills Page Controller
  async function initSearch() {
    initCommon();
    const searchForm = document.getElementById('skillsSearchFilterForm');
    const resultsContainer = document.getElementById('searchResultsContainer');

    const executeSearch = async () => {
      if (!resultsContainer) return;
      resultsContainer.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Searching skills...</p></div>`;

      const kw = document.getElementById('searchKeywordInput')?.value.trim() || '';
      const type = document.getElementById('searchTypeFilter')?.value || '';
      const prof = document.getElementById('searchProficiencyFilter')?.value || '';

      let query = `${API.search}?keyword=${encodeURIComponent(kw)}`;
      if (type) query += `&type=${encodeURIComponent(type)}`;
      if (prof) query += `&proficiency=${encodeURIComponent(prof)}`;

      try {
        const res = await request(query);
        if (!res.success || !res.data || res.data.length === 0) {
          resultsContainer.innerHTML = `
            <div class="empty-state-box">
              <h4>No skills matched your search</h4>
              <p>Try different keywords or reset the type and proficiency filters.</p>
            </div>`;
          return;
        }

        const list = res.data;
        let html = `<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(300px, 1fr)); gap:20px;">`;
        list.forEach(s => {
          const isTeach = s.skillType === 'TEACH';
          const isSelf = s.studentId === activeStudentId;
          html += `
            <div class="card" style="display:flex; flex-direction:column; justify-content:space-between;">
              <div>
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                  <span class="badge ${isTeach ? 'badge-teach' : 'badge-learn'}">${s.skillType}</span>
                  <span class="badge badge-${s.proficiencyLevel.toLowerCase()}">${s.proficiencyLevel}</span>
                </div>
                <h3 style="font-size:1.2rem; font-weight:800; color:var(--deep-forest); margin-bottom:4px;">${s.skillName}</h3>
                <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:10px;">
                  Registered by <strong>${s.studentName}</strong> ${isSelf ? '(You)' : ''}
                </div>
                <p style="font-size:0.88rem; color:var(--text-light); margin-bottom:16px;">
                  ${s.description ? s.description : 'No description provided.'}
                </p>
              </div>
              <div style="border-top:1px solid var(--border-light); padding-top:14px; display:flex; justify-content:space-between; align-items:center;">
                <span style="font-size:0.78rem; color:var(--text-muted);">Student ID #${s.studentId}</span>
                ${!isSelf ? `
                  <button class="btn btn-primary btn-sm" onclick="SkillSwap.promptSendRequestToStudent(${s.studentId}, '${s.studentName}', ${s.skillId}, '${s.skillName}', '${s.skillType}')">
                    Connect &rarr;
                  </button>
                ` : `<span style="font-size:0.8rem; font-weight:600; color:var(--text-muted);">Your Skill</span>`}
              </div>
            </div>`;
        });
        html += `</div>`;
        resultsContainer.innerHTML = html;
      } catch (e) {
        resultsContainer.innerHTML = `<div class="empty-state-box"><h4>Search failed</h4><p>${e.message}</p></div>`;
      }
    };

    if (searchForm) {
      searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        executeSearch();
      });
      document.getElementById('searchTypeFilter')?.addEventListener('change', executeSearch);
      document.getElementById('searchProficiencyFilter')?.addEventListener('change', executeSearch);
    }

    // Check URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const initialKw = urlParams.get('keyword');
    if (initialKw && document.getElementById('searchKeywordInput')) {
      document.getElementById('searchKeywordInput').value = initialKw;
    }
    executeSearch();
  }

  // F. Smart Skill Matching Page Controller
  async function initMatches() {
    initCommon();
    const container = document.getElementById('matchesListContainer');
    if (!container) return;

    container.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Finding reciprocal skill exchange partners...</p></div>`;

    try {
      const res = await request(API.studentMatches(activeStudentId));
      if (!res.success || !res.data || res.data.length === 0) {
        container.innerHTML = `
          <div class="empty-state-box">
            <div class="empty-state-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="17 1 21 5 17 9"></polyline>
                <path d="M3 11V9a4 4 0 0 1 4-4h14"></path>
                <polyline points="7 23 3 19 7 15"></polyline>
                <path d="M21 13v2a4 4 0 0 1-4 4H3"></path>
              </svg>
            </div>
            <h4>No mutual matches found for your profile</h4>
            <p>A mutual match occurs when another student teaches what you want to learn, AND wants to learn what you teach!</p>
            <div style="display:flex; justify-content:center; gap:12px; margin-top:16px;">
              <a href="/skills?studentId=${activeStudentId}" class="btn btn-primary">Add More Skills</a>
              <button class="btn btn-secondary" onclick="SkillSwap.loadAllPlatformMatches()">View All Platform Matches</button>
            </div>
          </div>`;
        return;
      }

      const matches = res.data;
      renderMatchesCards(container, matches);
    } catch (e) {
      container.innerHTML = `<div class="empty-state-box"><h4>Error calculating smart matches</h4><p>${e.message}</p></div>`;
    }
  }

  function renderMatchesCards(container, matches) {
    let html = `<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(360px, 1fr)); gap:24px;">`;
    matches.forEach(m => {
      html += `
        <div class="card" style="border:1.5px solid var(--primary-mint); box-shadow:var(--shadow-md);">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; border-bottom:1px solid var(--border-light); padding-bottom:12px;">
            <div>
              <div style="font-size:1.15rem; font-weight:800; color:var(--deep-forest);">${m.partnerStudentName}</div>
              <div style="font-size:0.8rem; color:var(--text-muted);">${m.partnerEmail} &bull; ID #${m.partnerStudentId}</div>
            </div>
            <span class="badge badge-intermediate">MUTUAL MATCH</span>
          </div>

          <div style="background:#FAF8F1; border:1px solid var(--border-color); border-radius:var(--radius-md); padding:16px; margin-bottom:18px;">
            <div style="margin-bottom:12px;">
              <span style="font-size:0.75rem; text-transform:uppercase; font-weight:700; color:var(--text-muted);">You Teach (They Learn):</span>
              <div style="display:flex; justify-content:space-between; align-items:center; margin-top:4px;">
                <strong style="color:var(--deep-forest); font-size:1rem;">${m.currentOfferedSkillName}</strong>
                <span class="badge badge-${m.currentOfferedProficiency.toLowerCase()}">${m.currentOfferedProficiency}</span>
              </div>
            </div>
            <div style="border-top:1px dashed var(--border-color); padding-top:10px;">
              <span style="font-size:0.75rem; text-transform:uppercase; font-weight:700; color:var(--text-muted);">They Teach (You Learn):</span>
              <div style="display:flex; justify-content:space-between; align-items:center; margin-top:4px;">
                <strong style="color:#8A4315; font-size:1rem;">${m.partnerOfferedSkillName}</strong>
                <span class="badge badge-${m.partnerOfferedProficiency.toLowerCase()}">${m.partnerOfferedProficiency}</span>
              </div>
            </div>
          </div>

          <button class="btn btn-forest" style="width:100%;" 
                  onclick="SkillSwap.sendExchangePrompt(${m.currentStudentId}, ${m.partnerStudentId}, ${m.currentOfferedSkillId}, ${m.partnerOfferedSkillId}, '${m.partnerStudentName}')">
            Send Skill Exchange Request &rarr;
          </button>
        </div>`;
    });
    html += `</div>`;
    container.innerHTML = html;
  }

  async function loadAllPlatformMatches() {
    const container = document.getElementById('matchesListContainer');
    container.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Loading all platform matches...</p></div>`;
    try {
      const res = await request(API.matches);
      if (res.success && res.data && res.data.length > 0) {
        renderMatchesCards(container, res.data);
      } else {
        container.innerHTML = `<div class="empty-state-box"><h4>No matches currently in database</h4></div>`;
      }
    } catch (e) {
      showToast(e.message, 'error');
    }
  }

  // G. Exchange Requests Page Controller
  async function initRequests() {
    initCommon();
    loadRequestsTab('received');
  }

  let activeRequestsTab = 'received';

  async function loadRequestsTab(tab) {
    activeRequestsTab = tab;
    const container = document.getElementById('requestsListContainer');
    if (!container) return;

    document.querySelectorAll('.request-tab-btn').forEach(btn => {
      btn.classList.toggle('btn-forest', btn.dataset.tab === tab);
      btn.classList.toggle('btn-secondary', btn.dataset.tab !== tab);
    });

    container.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Loading requests...</p></div>`;

    try {
      const url = tab === 'received' ? API.receivedRequests(activeStudentId) : API.sentRequests(activeStudentId);
      const res = await request(url);

      if (!res.success || !res.data || res.data.length === 0) {
        container.innerHTML = `
          <div class="empty-state-box">
            <h4>No ${tab} exchange requests</h4>
            <p>${tab === 'received' ? 'You have not received any swap requests yet.' : 'You have not sent any swap requests yet.'}</p>
            <a href="/matches?studentId=${activeStudentId}" class="btn btn-primary btn-sm">Find Exchange Partners</a>
          </div>`;
        return;
      }

      const list = res.data;
      let html = `<div style="display:flex; flex-direction:column; gap:16px;">`;

      list.forEach(r => {
        const isReceiver = r.receiverId === activeStudentId;
        const status = r.status;
        const statusClass = `status-${status.toLowerCase()}`;

        html += `
          <div class="card" style="border:1px solid var(--border-color); display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:18px;">
            <div style="flex:1; min-width:280px;">
              <div style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
                <span class="status-badge ${statusClass}">${status}</span>
                <span style="font-size:0.8rem; color:var(--text-muted); font-weight:600;">Request #${r.requestId} &bull; ${r.requestDate ? r.requestDate.substring(0, 16) : ''}</span>
              </div>
              <div style="font-size:1.1rem; font-weight:800; color:var(--deep-forest); margin-bottom:6px;">
                ${isReceiver ? `Received from ${r.requesterName}` : `Sent to ${r.receiverName}`}
              </div>
              <div style="font-size:0.9rem; color:var(--text-light); line-height:1.6;">
                <span>&bull; Offered Skill: <strong>${r.offeredSkillName}</strong> [${r.offeredSkillProficiency}]</span><br>
                <span>&bull; Wanted Skill: <strong>${r.wantedSkillName}</strong> [${r.wantedSkillProficiency}]</span>
              </div>
            </div>

            <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
              ${isReceiver && status === 'PENDING' ? `
                <button class="btn btn-primary btn-sm" onclick="SkillSwap.handleRequestAction(${r.requestId}, 'accept')">Accept Swap</button>
                <button class="btn btn-danger btn-sm" onclick="SkillSwap.handleRequestAction(${r.requestId}, 'reject')">Reject</button>
              ` : ''}

              ${status === 'ACCEPTED' ? `
                <button class="btn btn-forest btn-sm" onclick="SkillSwap.handleRequestAction(${r.requestId}, 'complete')">Mark Completed</button>
              ` : ''}

              ${(status === 'PENDING' || status === 'ACCEPTED') ? `
                <button class="btn btn-secondary btn-sm" onclick="SkillSwap.handleRequestAction(${r.requestId}, 'cancel')">Cancel</button>
              ` : ''}
            </div>
          </div>`;
      });

      html += `</div>`;
      container.innerHTML = html;

    } catch (e) {
      container.innerHTML = `<div class="empty-state-box"><h4>Error loading requests</h4><p>${e.message}</p></div>`;
    }
  }

  async function handleRequestAction(requestId, action) {
    let confirmMsg = `Are you sure you want to ${action} Request #${requestId}?`;
    if (action === 'complete') confirmMsg = `Confirm that both skills have been exchanged successfully for Request #${requestId}?`;

    showModal({
      title: `${action.toUpperCase()} Request`,
      content: `<p>${confirmMsg}</p>`,
      confirmText: action.toUpperCase(),
      onConfirm: async () => {
        let endpoint;
        if (action === 'accept') endpoint = API.acceptRequest(requestId);
        else if (action === 'reject') endpoint = API.rejectRequest(requestId);
        else if (action === 'cancel') endpoint = API.cancelRequest(requestId);
        else if (action === 'complete') endpoint = API.completeRequest(requestId);

        const res = await request(endpoint, {
          method: 'PUT',
          body: JSON.stringify({ studentId: activeStudentId })
        });

        if (res.success) {
          showToast(`Request #${requestId} has been ${action}ed!`, 'success');
          loadRequestsTab(activeRequestsTab);
        }
      }
    });
  }

  // H. Student Directory Page Controller
  async function initStudents() {
    initCommon();
    const container = document.getElementById('studentsDirectoryContainer');
    if (!container) return;

    container.innerHTML = `<div class="loading-box"><div class="spinner"></div><p>Loading student directory...</p></div>`;

    try {
      const res = await request(API.students);
      if (!res.success || !res.data || res.data.length === 0) {
        container.innerHTML = `<div class="empty-state-box"><h4>No registered students found</h4></div>`;
        return;
      }

      const students = res.data;
      let html = `<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(300px, 1fr)); gap:20px;">`;
      
      students.forEach(s => {
        const initials = s.studentName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
        const isCurrent = s.studentId === activeStudentId;
        html += `
          <div class="card" style="display:flex; flex-direction:column; justify-content:space-between;">
            <div>
              <div style="display:flex; align-items:center; gap:14px; margin-bottom:14px;">
                <div class="profile-avatar-circle" style="width:48px; height:48px; font-size:1.1rem;">${initials}</div>
                <div>
                  <h3 style="font-size:1.1rem; font-weight:800; color:var(--deep-forest);">${s.studentName}</h3>
                  <p style="font-size:0.8rem; color:var(--text-muted);">${s.email}</p>
                </div>
              </div>
              <div style="font-size:0.85rem; color:var(--text-light); margin-bottom:14px;">
                <strong>Contact:</strong> ${s.contactInfo || 'N/A'}
              </div>
            </div>
            <div style="border-top:1px solid var(--border-light); padding-top:12px; display:flex; justify-content:space-between; align-items:center;">
              <span style="font-size:0.78rem; color:var(--text-muted);">Student ID #${s.studentId}</span>
              ${isCurrent ? 
                '<span class="badge badge-intermediate">YOU</span>' :
                `<button class="btn btn-forest btn-sm" onclick="SkillSwap.switchStudent(${s.studentId})">View As &rarr;</button>`
              }
            </div>
          </div>`;
      });

      html += `</div>`;
      container.innerHTML = html;
    } catch (e) {
      container.innerHTML = `<div class="empty-state-box"><h4>Error loading directory</h4><p>${e.message}</p></div>`;
    }
  }

  // Helper: Prompt send exchange request dialog
  function sendExchangePrompt(requesterId, receiverId, offeredSkillId, wantedSkillId, partnerName) {
    showModal({
      title: 'Send Skill Exchange Request',
      content: `
        <p>Send a peer-to-peer exchange request to <strong>${partnerName}</strong>?</p>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-top:8px;">
          They will be notified and can accept to begin trading skills!
        </p>`,
      confirmText: 'Send Request',
      onConfirm: async () => {
        const res = await request(API.requests, {
          method: 'POST',
          body: JSON.stringify({
            requesterId,
            receiverId,
            offeredSkillId,
            wantedSkillId
          })
        });

        if (res.success) {
          showToast(`Exchange Request #${res.data.requestId} sent to ${partnerName}!`, 'success');
          setTimeout(() => {
            window.location.href = `/requests?studentId=${activeStudentId}`;
          }, 1200);
        }
      }
    });
  }

  // Connect helper from search
  async function promptSendRequestToStudent(receiverId, receiverName, wantedSkillId, wantedSkillName, skillType) {
    if (skillType !== 'TEACH') {
      showToast(`${receiverName} wants to LEARN '${wantedSkillName}'. You can only request skills that other students TEACH.`, 'warning');
      return;
    }

    try {
      // Fetch my TEACH skills to offer
      const mySkillsRes = await request(API.studentSkills(activeStudentId));
      const myTeachSkills = (mySkillsRes.data || []).filter(s => s.skillType === 'TEACH');

      if (myTeachSkills.length === 0) {
        showToast('You must register at least one TEACH skill to offer in exchange!', 'warning');
        return;
      }

      let selectOptions = myTeachSkills.map(s => 
        `<option value="${s.skillId}">${s.skillName} (${s.proficiencyLevel})</option>`
      ).join('');

      let content = `
        <p style="margin-bottom:14px;">Select the skill you will teach <strong>${receiverName}</strong> in exchange for <strong>${wantedSkillName}</strong>:</p>
        <div class="form-group">
          <label class="form-label">Your Offered Skill:</label>
          <select id="modalOfferedSkillSelect" class="form-control">
            ${selectOptions}
          </select>
        </div>`;

      showModal({
        title: `Propose Swap with ${receiverName}`,
        content,
        confirmText: 'Propose Swap',
        onConfirm: async () => {
          const offeredId = parseInt(document.getElementById('modalOfferedSkillSelect').value);
          const res = await request(API.requests, {
            method: 'POST',
            body: JSON.stringify({
              requesterId: activeStudentId,
              receiverId,
              offeredSkillId: offeredId,
              wantedSkillId
            })
          });

          if (res.success) {
            showToast(`Swap request sent to ${receiverName}!`, 'success');
          }
        }
      });
    } catch (e) {
      showToast(e.message, 'error');
    }
  }

  return {
    getActiveStudentId,
    toast: showToast,
    modal: showModal,
    closeModal,
    promptSwitchStudent,
    switchStudent,
    initDashboard,
    initRegister,
    initProfile,
    initSkills,
    initSearch,
    initMatches,
    initRequests,
    initStudents,
    sendExchangePrompt,
    promptSendRequestToStudent,
    handleRequestAction,
    loadRequestsTab,
    confirmDeleteSkill,
    loadAllPlatformMatches
  };
})();
