(function () {
    var storageKey = 'todoapp-theme';
    var root = document.documentElement;

    function getPreferredTheme() {
        var storedTheme = localStorage.getItem(storageKey);

        if (storedTheme === 'light' || storedTheme === 'dark') {
            return storedTheme;
        }

        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function setTheme(theme) {
        root.setAttribute('data-bs-theme', theme);
        localStorage.setItem(storageKey, theme);
        updateToggle(theme);
    }

    function updateToggle(theme) {
        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            var isDark = theme === 'dark';
            button.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
            button.setAttribute('title', isDark ? 'Switch to light mode' : 'Switch to dark mode');
        });
    }

    root.setAttribute('data-bs-theme', getPreferredTheme());

    window.addEventListener('DOMContentLoaded', function () {
        updateToggle(root.getAttribute('data-bs-theme'));

        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            button.addEventListener('click', function () {
                var nextTheme = root.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
                setTheme(nextTheme);
            });
        });
    });
})();
